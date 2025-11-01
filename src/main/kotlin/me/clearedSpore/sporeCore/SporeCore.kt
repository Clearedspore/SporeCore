package me.clearedSpore.sporeCore

import co.aikar.commands.BaseCommand
import co.aikar.commands.Locales
import co.aikar.commands.MessageKeys
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.ChatInput
import me.clearedSpore.sporeAPI.util.Cooldown
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeCore.acf.ConfirmCondition
import me.clearedSpore.sporeCore.acf.CooldownCondition
import me.clearedSpore.sporeCore.commands.*
import me.clearedSpore.sporeCore.commands.currency.CurrencyCommand
import me.clearedSpore.sporeCore.commands.currency.CurrencyShopCommand
import me.clearedSpore.sporeCore.commands.economy.BalTopCommand
import me.clearedSpore.sporeCore.commands.economy.EcoLogsCommand
import me.clearedSpore.sporeCore.commands.economy.EconomyCommand
import me.clearedSpore.sporeCore.commands.economy.PayCommand
import me.clearedSpore.sporeCore.commands.gamemode.*
import me.clearedSpore.sporeCore.commands.home.CreateHomeCommand
import me.clearedSpore.sporeCore.commands.home.DelHomeCommand
import me.clearedSpore.sporeCore.commands.home.HomeCommand
import me.clearedSpore.sporeCore.commands.privatemessages.PrivateMessageCommand
import me.clearedSpore.sporeCore.commands.privatemessages.ReplyCommand
import me.clearedSpore.sporeCore.commands.spawn.SetSpawnCommand
import me.clearedSpore.sporeCore.commands.spawn.SpawnCommand
import me.clearedSpore.sporeCore.commands.teleport.*
import me.clearedSpore.sporeCore.commands.utilitymenus.*
import me.clearedSpore.sporeCore.currency.CurrencySystemService
import me.clearedSpore.sporeCore.database.Database
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.eco.VaultEco
import me.clearedSpore.sporeCore.features.homes.HomeService
import me.clearedSpore.sporeCore.features.kit.KitService
import me.clearedSpore.sporeCore.features.stats.PlaytimeTracker
import me.clearedSpore.sporeCore.features.warp.WarpService
import me.clearedSpore.sporeCore.hook.PlaceholderAPIHook
import me.clearedSpore.sporeCore.listener.ChatEvent
import me.clearedSpore.sporeCore.listener.DeathListener
import me.clearedSpore.sporeCore.listener.LoggerEvent
import me.clearedSpore.sporeCore.user.UserListener
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.reflect.Field
import kotlin.math.sin


class SporeCore : JavaPlugin() {

    companion object {
        lateinit var instance: SporeCore
    }

    lateinit var commandManager: PaperCommandManager
    lateinit var chatInput: ChatInput

    lateinit var coreConfig: CoreConfig
    lateinit var database: Database

    lateinit var warpService: WarpService
    lateinit var homeService: HomeService
    lateinit var kitService: KitService

    var eco: Economy? = null

    override fun onEnable() {
        instance = this
        coreConfig = loadConfig()
        Logger.initialize(coreConfig.general.prefix)
        Logger.info("Loading SporeCore")
        Message.init(true)
        commandManager = PaperCommandManager(this)
        setupACF()
        Task.initialize(this)
        Perm.registerAll()
        chatInput = ChatInput(this)

        setupEconomy()

        if(coreConfig.features.currency.enabled){
            CurrencySystemService.initialize()
        }

        DatabaseManager.init(dataFolder)
        database = DatabaseManager.getServerData()
        server.pluginManager.registerEvents(UserListener(), this)

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPIHook().register()
            Logger.info("Successfully integrated with PlaceholderAPI")
        }

        if(coreConfig.features.warps){
            warpService = WarpService()
        }

        if(coreConfig.features.homes){
            homeService = HomeService()
        }

        if(coreConfig.features.kits){
            kitService = KitService()
        }

        PlaytimeTracker.start()
        Cooldown.createCooldown("msg_cooldown", 2)

        registerListeners()
        registerCompletions()
        registerCommands()

        logStartupBanner()
    }

    override fun onDisable() {
        PlaytimeTracker.stop()
        Logger.infoDB("Saving all user data before shutdown...")
        UserManager.saveAllUsers()
        Logger.infoDB("All user data saved. Goodbye!")

        DatabaseManager.close()
    }


    fun setupEconomy(){
        if (coreConfig.economy.enabled) {
            if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
                Logger.warn("Vault not found! Disabling economy.")
                coreConfig.economy.enabled = false
            } else {
                val vaultEco = VaultEco()
                Bukkit.getServicesManager().register(
                    Economy::class.java,
                    vaultEco,
                    this,
                    ServicePriority.Highest
                )

                EconomyService.reloadAsync()

                Logger.info("Registered Vault economy: ${coreConfig.economy.name}")
            }
        }
    }

    fun registerListeners(){
        server.pluginManager.registerEvents(LoggerEvent(), this)
        server.pluginManager.registerEvents(ChatEvent(), this)
        server.pluginManager.registerEvents(DeathListener(), this)
    }

    fun loadConfig(): CoreConfig {
        val configFile = File(dataFolder, "config.yml").toPath()
        return try {
            YamlConfigurations.update(configFile, CoreConfig::class.java)
        } catch (ex: ConfigurationException) {
            Logger.error("Invalid config detected. Some values will use defaults. Check console for details.")

            Logger.error("Configuration error while loading config.yml: ${ex.message}")
            ex.printStackTrace()

            CoreConfig()
        }
    }

    fun logStartupBanner() {
        val pluginName = "SporeCore"
        val version = "v${description.version}"
        val author = "ClearedSpore"
        val serverType = Bukkit.getServer().name + " - " + Bukkit.getServer().version

        val features = mutableListOf<String>()
        if (::warpService.isInitialized) features.add("§aWarps")
        if (::homeService.isInitialized) features.add("§bHomes")
        if (coreConfig.economy.enabled) features.add("§eEconomy")
        if(::kitService.isInitialized) features.add("§9Kits")
        val featureLine = if (features.isNotEmpty()) features.joinToString(" §7| ") else "§7No features enabled"

        val banner = listOf(
            "",
            "   _____  _____ ".blue(),
            "  / ____|/ ____|".blue(),
            " | (___ | |     ".blue(),
            "  \\___ \\| |     ".blue(),
            "  ____) | |____ ".blue(),
            " |_____/ \\_____|".blue(),
            "",
            "§f$pluginName §e$version",
            "§fAuthor: $author",
            "§fServer: $serverType",
            "§fFeatures: $featureLine",
            ""
        )

        banner.forEach { line ->
            Bukkit.getConsoleSender().sendMessage(line)
        }
    }


    fun setupACF() {
        val prefix = "⚙ ".blue() + "SporeCore » ".white()

        val locales = commandManager.locales

        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_HEADER, "$prefix &fAvailable Commands:")
        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_FORMAT, "&e/{command} &7{parameters} &f- {description}")

        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_DETAILED_HEADER, "$prefix &fCommand Help for &e/{command}")
        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_DETAILED_COMMAND_FORMAT, "Usage: &f/{command} {parameters}".blue())
        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_DETAILED_PARAMETER_FORMAT, "&7- &f{parameter} &7({description})")

        locales.addMessage(Locales.ENGLISH, MessageKeys.INVALID_SYNTAX, "$prefix" + "Use &e{command} &f{syntax}".blue())
        locales.addMessage(Locales.ENGLISH, MessageKeys.PERMISSION_DENIED, "$prefix" + "You don't have permission to use this command!".red())
        locales.addMessage(Locales.ENGLISH, MessageKeys.UNKNOWN_COMMAND, "$prefix" + "That command does not exist!".red())

        ConfirmCondition.register(commandManager)
        CooldownCondition.register(commandManager)
    }

    fun registerCommands(){

        val features = coreConfig.features

        commandManager.registerDependency(WarpService::class.java, warpService)
        commandManager.registerDependency(HomeService::class.java, homeService)
        commandManager.registerDependency(KitService::class.java, kitService)

        commandManager.registerCommand(AdventureCommand())
        commandManager.registerCommand(CreativeCommand())
        commandManager.registerCommand(GamemodeCommand())
        commandManager.registerCommand(SpectatorCommand())
        commandManager.registerCommand(SurvivalCommand())

        if(features.teleportRequest) {
            commandManager.registerCommand(TpaAcceptCommand())
            commandManager.registerCommand(TpaCommand())
            commandManager.registerCommand(TpaHereCommand())
            commandManager.registerCommand(TpaDenyCommand())
        }

        commandManager.registerCommand(TeleportCommand())
        commandManager.registerCommand(TpAllCommand())
        commandManager.registerCommand(TpCoordsCommand())
        commandManager.registerCommand(TphereCommand())

        commandManager.registerCommand(HealCommand())
        commandManager.registerCommand(FeedCommand())
        commandManager.registerCommand(RepairCommand())
        commandManager.registerCommand(RepairAllCommand())
        commandManager.registerCommand(FlyCommand())
        commandManager.registerCommand(GodCommand())
        commandManager.registerCommand(ClearinvCommand())

        if(features.privateMessages){
            commandManager.registerCommand(PrivateMessageCommand())
            commandManager.registerCommand(ReplyCommand())
        }

        if(features.utilityMenus){
            commandManager.registerCommand(AnvilCommand())
            commandManager.registerCommand(CartographyTableCommand())
            commandManager.registerCommand(EnchantmentTableCommand())
            commandManager.registerCommand(GrindstoneCommand())
            commandManager.registerCommand(LoomCommand())
            commandManager.registerCommand(SmithingTableCommand())
            commandManager.registerCommand(StoneCutterCommand())
            commandManager.registerCommand(WorkbenchCommand())
        }

        if(features.utilityMenus){
            commandManager.registerCommand(SpawnCommand())
            commandManager.registerCommand(SetSpawnCommand())
        }

        commandManager.registerCommand(CoreCommand())

        if(features.settings) {
            commandManager.registerCommand(SettingCommand())
        }

        if(features.warps){
            commandManager.registerCommand(WarpCommand())
        }

        if(features.homes){
            commandManager.registerCommand(HomeCommand())
            commandManager.registerCommand(CreateHomeCommand())
            commandManager.registerCommand(DelHomeCommand())
        }

        if(coreConfig.economy.enabled){
            commandManager.registerCommand(EconomyCommand())
            commandManager.registerCommand(PayCommand())
            commandManager.registerCommand(BalTopCommand())
            commandManager.registerCommand(EcoLogsCommand())
        }

        commandManager.registerCommand(PlayerTimeCommand())
        commandManager.registerCommand(PlayerWeatherCommand())

        if(features.kits){
            commandManager.registerCommand(KitCommand())
        }

        if(features.stats){
            commandManager.registerCommand(StatsCommand())
        }


        if (features.currency.enabled) {
            val singular = CurrencySystemService.config.currencySettings.singularName.lowercase()
            val plural = CurrencySystemService.config.currencySettings.pluralName.lowercase()
            val aliases = listOf("currency", singular, plural)

            for (alias in aliases.distinct()) {
                commandManager.commandReplacements.addReplacement("currencyalias", alias)
                commandManager.registerCommand(CurrencyCommand())
            }

            val shopAliases = CurrencySystemService.config.currencySettings.shopCommand

            if (shopAliases.isNotEmpty()) {
                for (alias in shopAliases.distinct()) {
                    commandManager.commandReplacements.addReplacement("currencyshopalias", alias)
                    commandManager.registerCommand(CurrencyShopCommand())
                }
            }

            Logger.info("Registered currency aliases: ${aliases.joinToString(", ")}")
        }

    }


    fun registerCompletions(){
        commandManager.commandCompletions.registerCompletion("gamemodes") { context ->
            val sender = context.sender
            GameMode.values()
                .filter { gm ->
                    val perm = when (gm) {
                        GameMode.CREATIVE -> Perm.CREATIVE
                        GameMode.SURVIVAL -> Perm.SURVIVAL
                        GameMode.SPECTATOR -> Perm.SPECTATOR
                        GameMode.ADVENTURE -> Perm.ADVENTURE
                    }
                    sender.hasPermission(perm)
                }
                .map { it.name.lowercase() }
        }

        commandManager.commandCompletions.registerCompletion("warps") { context ->
            val player = context.player
            if (player == null) return@registerCompletion emptyList<String>()

            try {
                warpService.getAllWarps()
                    .filter { warp ->
                        warp.permission == null || player.hasPermission(warp.permission)
                    }
                    .map { it.name }
            } catch (e: Exception) {
                emptyList<String>()
            }
        }

        commandManager.commandCompletions.registerCompletion("kits") { context ->
            val player = context.player
            if (player == null) return@registerCompletion emptyList<String>()

            try {
                kitService.getAllKits()
                    .filter { kit ->
                        kit.permission == null || player.hasPermission(kit.permission)
                    }
                    .map { it.name }
            } catch (e: Exception) {
                emptyList<String>()
            }
        }

        commandManager.commandCompletions.registerCompletion("payamounts") { context ->
            val input = context.input.lowercase().trim()
            val number = input.toDoubleOrNull() ?: return@registerCompletion emptyList<String>()

            val df = java.text.DecimalFormat("#.##")
            val formatted = df.format(number)

            listOf(
                "${formatted}k",
                "${formatted}m",
                "${formatted}b",
                "${formatted}t"
            )
        }


        commandManager.commandCompletions.registerCompletion("homes") { context ->
            val sender = context.sender
            if (sender !is Player) return@registerCompletion emptyList()

            val user = UserManager.get(sender)

            if(user == null){
                return@registerCompletion emptyList()
            }

            user.homes.map { it.name }
        }

        commandManager.commandCompletions.registerCompletion("playerhomes") { context ->
            val targetName = context.input
            val target = Bukkit.getOfflinePlayer(targetName) ?: return@registerCompletion emptyList()
            val user = UserManager.getIfLoaded(target.uniqueId) ?: return@registerCompletion emptyList()
            user.homes.map { it.name }
        }

        commandManager.commandCompletions.registerCompletion("materials") { context ->
            val input = context.input?.trim()?.uppercase() ?: ""

            Material.entries
                .asSequence()
                .filter { !it.isLegacy }
                .map { it.name.lowercase().capitalizeFirstLetter() }
                .filter { it!!.startsWith(input.lowercase()) }
                .take(50)
                .toList()
        }

    }
}
