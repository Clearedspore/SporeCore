package me.clearedSpore.sporeCore

import co.aikar.commands.*
import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.acf.ConfirmCondition
import me.clearedSpore.sporeCore.acf.CooldownCondition
import me.clearedSpore.sporeCore.acf.ItemCompletions.registerItemCompletions
import me.clearedSpore.sporeCore.acf.TargetSelectorResolver
import me.clearedSpore.sporeCore.acf.TargetType
import me.clearedSpore.sporeCore.acf.error.PlayerOnlyResolver
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetEntities
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetSinglePlayer
import me.clearedSpore.sporeCore.acf.targets.resolver.AnyTargetResolver
import me.clearedSpore.sporeCore.acf.targets.resolver.SinglePlayerResolver
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.commands.*
import me.clearedSpore.sporeCore.commands.channel.ChannelCommand
import me.clearedSpore.sporeCore.commands.currency.CurrencyCommand
import me.clearedSpore.sporeCore.commands.currency.CurrencyShopCommand
import me.clearedSpore.sporeCore.commands.discord.LinkCommand
import me.clearedSpore.sporeCore.commands.discord.UnLinkCommand
import me.clearedSpore.sporeCore.commands.economy.BalTopCommand
import me.clearedSpore.sporeCore.commands.economy.EcoLogsCommand
import me.clearedSpore.sporeCore.commands.economy.EconomyCommand
import me.clearedSpore.sporeCore.commands.economy.PayCommand
import me.clearedSpore.sporeCore.commands.gamemode.*
import me.clearedSpore.sporeCore.commands.home.CreateHomeCommand
import me.clearedSpore.sporeCore.commands.home.DelHomeCommand
import me.clearedSpore.sporeCore.commands.home.HomeCommand
import me.clearedSpore.sporeCore.commands.inventory.InvRollbackCommand
import me.clearedSpore.sporeCore.commands.inventory.InventoryManagerCommand
import me.clearedSpore.sporeCore.commands.moderation.*
import me.clearedSpore.sporeCore.commands.moderation.mode.CustomModeCommand
import me.clearedSpore.sporeCore.commands.moderation.mode.ModeCommand
import me.clearedSpore.sporeCore.commands.privatemessages.PrivateMessageCommand
import me.clearedSpore.sporeCore.commands.privatemessages.ReplyCommand
import me.clearedSpore.sporeCore.commands.spawn.SetSpawnCommand
import me.clearedSpore.sporeCore.commands.spawn.SpawnCommand
import me.clearedSpore.sporeCore.commands.teleport.*
import me.clearedSpore.sporeCore.commands.util.*
import me.clearedSpore.sporeCore.commands.utilitymenus.*
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService
import me.clearedSpore.sporeCore.features.currency.CurrencySystemService
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.eco.VaultEco
import me.clearedSpore.sporeCore.features.homes.HomeService
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationPriority
import me.clearedSpore.sporeCore.features.kit.KitService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.features.mode.listener.ModeListener
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.setting.SettingRegistry
import me.clearedSpore.sporeCore.features.stats.PlaytimeTracker
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.features.warp.WarpService
import me.clearedSpore.sporeCore.hook.PlaceholderAPIHook
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.listener.InventoryListener
import me.clearedSpore.sporeCore.task.ActionBarTicker
import me.clearedSpore.sporeCore.task.TpsTask
import me.clearedSpore.sporeCore.task.VanishTask
import me.clearedSpore.sporeCore.user.UserListener
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ListenerRegistry
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.UpdateChecker
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile


class SporeCore : JavaPlugin() {

    companion object {
        lateinit var instance: SporeCore
    }

    lateinit var commandManager: PaperCommandManager

    lateinit var coreConfig: CoreConfig
    lateinit var database: Database

    lateinit var listener: ListenerRegistry

    lateinit var resolver: TargetSelectorResolver
    lateinit var settingRegistry: SettingRegistry
    lateinit var warpService: WarpService
    lateinit var homeService: HomeService
    lateinit var kitService: KitService
    lateinit var updateChecker: UpdateChecker

    var chat: Chat? = null
    var perms: Permission? = null
    var eco: Economy? = null

    var totalCommands: Int = 0
    var discordEnabled: Boolean = false

    var freshStartup = true


    override fun onEnable() {
        totalCommands = 0
        instance = this
        coreConfig = loadConfig()
        Logger.initialize(
            coreConfig.general.prefix,
            "This should not be happening. Please contact the developer ClearedSpore."
        )

        setupEconomy()
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            setupChat()
            setupPermissions()
        } else {
            Logger.warn("Vault not installed. Disabling Chat and Permissions")
        }

        Logger.info("Loading SporeCore")
        Message.init(true)
        commandManager = PaperCommandManager(this)
        setupACF()
        Task.onInitialize(this)
        Perm.registerAll()

        val pluginID = 28481
        Metrics(this, pluginID)

        updateChecker = UpdateChecker()

        CurrencySystemService.initialize()

        DatabaseManager.init(dataFolder)
        database = DatabaseManager.getServerData()
        server.pluginManager.registerEvents(UserListener(), this)

        InventoryManager.startCleanupTask()

        Task.run {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                PlaceholderAPIHook().register()
                Logger.info("Successfully integrated with PlaceholderAPI")
            }
        }

        ActionBarTicker.start()

        val features = coreConfig.features

        if (features.modes) {
            ModeService.initialize()

            server.pluginManager.registerEvents(
                ModeListener { player -> ModeService.getPlayerMode(player) },
                this
            )
        }

        if (features.settings) {
            settingRegistry = SettingRegistry(this)
            settingRegistry.loadAllSettings()
        }

        if (features.warps) {
            warpService = WarpService()
        }

        if (features.homes) {
            homeService = HomeService()
        }

        if (features.kits) {
            kitService = KitService()
        }

        if (coreConfig.discord.enabled) {
            loadDiscord()
        }

        if (features.vanish) {
            VanishTask.start()
        }

        TpsTask.start()

        if(features.reports){
            ReportService.startCleanupTask()
        }


        setupPunishments()

        PlaytimeTracker.start()
        Cooldown.createCooldown("msg_cooldown", 2)
        Cooldown.createCooldown("report", coreConfig.reports.reportCooldown)

        listener = ListenerRegistry(this)
        listener.registerAll()
        registerListeners()
        registerCompletions()
        registerCommands()

        logStartupBanner()
        Task.runLater({ freshStartup = false }, 20)
    }

    override fun onDisable() {
        val features = coreConfig.features

        if (features.modes) {
            ModeService.forceRestoreAllInventoriesOnShutdown()
        }

        if (features.vanish) {
            Logger.info("Unvanishing everyone....")
            val vanished = VanishService.vanishedPlayers.size
            VanishService.vanishedPlayers.forEach { uuid -> VanishService.unVanish(uuid) }
            Logger.info("Unvanished $vanished players")

            VanishTask.stop()
        }

        ChatInputService.clear()
        TpsTask.stop()
        LogsService.cleanupLogs()
        LogsService.stopCleanupTask()
        if(features.reports) {
            ReportService.cleanupReports()
            ReportService.stopCleanupTask()
        }
        ActionBarTicker.stop()
        InventoryManager.stopCleanupTask()


        PlaytimeTracker.stop()
        Logger.infoDB("Saving all user data before shutdown...")
        UserManager.saveAllUsers()
        Logger.infoDB("All user data saved. Goodbye!")

        DatabaseManager.close()
    }


    fun setupEconomy() {
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

    fun loadDiscord() {
        try {
            DiscordService.start()
        } catch (e: Exception) {
            Logger.error("Failed to startup discord bot!")
            e.printStackTrace()
        }

        registerCommand(LinkCommand())
        registerCommand(UnLinkCommand())
        discordEnabled = true
    }

    fun setupChat() {
        val rsp = server.servicesManager.getRegistration(Chat::class.java)
        chat = rsp?.provider
    }

    fun setupPunishments() {
        val enabled = coreConfig.features.punishments

        if (enabled) {
            registerCommand(PunishCommand())
            registerCommand(BanCmd())
            registerCommand(TempBanCmd())
            registerCommand(WarnCmd())
            registerCommand(TempWarnCmd())
            registerCommand(MuteCmd())
            registerCommand(TempMuteCmd())
            registerCommand(KickCmd())
            registerCommand(UnMuteCommand())
            registerCommand(UnBanCommand())
            registerCommand(HistoryCommand())
            registerCommand(AltsCommand())
            registerCommand(ClearChatCommand())
            PunishmentService.load()
        }
    }

    fun setupPermissions() {
        val rsp = server.servicesManager.getRegistration(Permission::class.java)
        perms = rsp?.provider
    }


    fun registerListeners() {
        var manualListeners = 0
        server.pluginManager.registerEvents(TpsTask, this)
        manualListeners++
        if (coreConfig.features.invRollback) {
            server.pluginManager.registerEvents(InventoryListener(), this)
            manualListeners++
        }

        Logger.info("Manually registered $manualListeners listeners")

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
        if (::kitService.isInitialized) features.add("§6Kits")
        if (PunishmentService.loaded) features.add("§cPunishments")
        if (DiscordService.initialized) features.add("§9Discord")
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

    fun registerCommands() {

        val features = coreConfig.features

        if (features.warps) {
            commandManager.registerDependency(WarpService::class.java, warpService)
        }
        if (features.homes) {
            commandManager.registerDependency(HomeService::class.java, homeService)
        }
        if (features.kits) {
            commandManager.registerDependency(KitService::class.java, kitService)
        }


        registerCommand(PendingMSGCommand())

        registerCommand(AdventureCommand())
        registerCommand(CreativeCommand())
        registerCommand(GamemodeCommand())
        registerCommand(SpectatorCommand())
        registerCommand(SurvivalCommand())

        if (features.teleportRequest) {
            registerCommand(TpaAcceptCommand())
            registerCommand(TpaCommand())
            registerCommand(TpaHereCommand())
            registerCommand(TpaDenyCommand())
        }

        registerCommand(TeleportCommand())
        registerCommand(TpAllCommand())
        registerCommand(TpCoordsCommand())
        registerCommand(TphereCommand())

        registerCommand(HealCommand())
        registerCommand(FeedCommand())
        registerCommand(RepairCommand())
        registerCommand(RepairAllCommand())
        registerCommand(FlyCommand())
        registerCommand(GodCommand())
        registerCommand(ClearinvCommand())

        if (features.privateMessages) {
            registerCommand(PrivateMessageCommand())
            registerCommand(ReplyCommand())
        }

        if (features.utilityMenus) {
            registerCommand(AnvilCommand())
            registerCommand(CartographyTableCommand())
            registerCommand(EnchantmentTableCommand())
            registerCommand(GrindstoneCommand())
            registerCommand(LoomCommand())
            registerCommand(SmithingTableCommand())
            registerCommand(StoneCutterCommand())
            registerCommand(WorkbenchCommand())
        }

        if (features.utilityMenus) {
            registerCommand(SpawnCommand())
            registerCommand(SetSpawnCommand())
        }

        registerCommand(CoreCommand())

        if (features.settings) {
            registerCommand(SettingCommand())
        }

        if (features.warps) {
            registerCommand(WarpCommand())
        }

        if (features.homes) {
            registerCommand(HomeCommand())
            registerCommand(CreateHomeCommand())
            registerCommand(DelHomeCommand())
        }

        if (coreConfig.economy.enabled) {
            registerCommand(EconomyCommand())
            registerCommand(PayCommand())
            registerCommand(BalTopCommand())
            registerCommand(EcoLogsCommand())
        }

        registerCommand(PlayerTimeCommand())
        registerCommand(PlayerWeatherCommand())

        if (features.kits) {
            registerCommand(KitCommand())
        }

        if (features.stats) {
            registerCommand(StatsCommand())
        }

        registerCommand(BackCommand())
        registerCommand(SpeedCommand())
        registerCommand(RebootCommand())
        registerCommand(BroadcastCommand())
        registerCommand(TrashCommand())
        registerCommand(EditItemCommand())
        registerCommand(ItemCommand())
        registerCommand(SudoCommand())
        registerCommand(PingCommand())
        registerCommand(WhoisCommand())
        registerCommand(GetLogsCommand())

        //util commands
        commandManager.registerCommand(UtilCommand())
        commandManager.registerCommand(UtilItemCommand())
        commandManager.registerCommand(UtilPlayerCommand())
        commandManager.registerCommand(UtilWorldCommand())
        commandManager.registerCommand(UtilServerCommand())
        commandManager.registerCommand(UtilInventoryCommand())

        if (coreConfig.chat.chatColor.enabled) {
            registerCommand(ChatColorCommand())
        }

        if (features.vanish) {
            registerCommand(VanishCommand())
        }

        if(features.investigation){
            registerCommand(InvestigationCommand())
        }

        if (features.modes) {
            registerCommand(ModeCommand())
            for (mode in ModeService.getModes()) {
                val cmd = CustomModeCommand(mode)

                val combinedAlias = mode.commands.joinToString("|")
                commandManager.commandReplacements.addReplacement("modealias", combinedAlias)

                registerCommand(cmd)
            }
        }

        if (features.invRollback) {
            registerCommand(InvRollbackCommand())
        }

        registerCommand(InventoryManagerCommand())
        registerCommand(FreezeCommand())
        registerCommand(TPSBarCommand())

        if(features.reports){
            registerCommand(ReportCommand())
        }

        if (features.channels) {
            for (channel in ChatChannelService.getChannels()) {
                val cmd = ChannelCommand(channel)
                cmd.requiredPermissions.add(channel.permission)

                val combinedAlias = channel.commands.joinToString("|")
                commandManager.commandReplacements.addReplacement("channelalias", combinedAlias)

                registerCommand(cmd)
                Logger.info("Registered ${channel.id} channel")
            }
        }

        if (features.currency.enabled) {
            val singular = CurrencySystemService.config.currencySettings.singularName.lowercase()
            val plural = CurrencySystemService.config.currencySettings.pluralName.lowercase()
            val aliases = listOf("currency", singular, plural)

            for (alias in aliases.distinct()) {
                commandManager.commandReplacements.addReplacement("currencyalias", alias)
                registerCommand(CurrencyCommand())
            }
            val shopAliases = CurrencySystemService.config.currencySettings.shopCommand

            if (shopAliases.isNotEmpty()) {
                for (alias in shopAliases.distinct()) {
                    commandManager.commandReplacements.addReplacement("currencyshopalias", alias)
                    registerCommand(CurrencyShopCommand())
                }
            }

            Logger.info("Registered currency aliases: ${aliases.joinToString(", ")}")
        }

        Logger.info("Loaded $totalCommands commands")

    }

    fun registerCommand(command: BaseCommand) {
        commandManager.registerCommand(command)
        totalCommands++
    }

    fun setupACF() {
        val prefix = "⚙ ".white() + "SporeCore &f» ".blue()

        val locales = commandManager.locales

        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_HEADER, "$prefix &fAvailable Commands:")
        locales.addMessage(
            Locales.ENGLISH,
            MessageKeys.HELP_FORMAT,
            "/{command} &7{parameters} &f- {description}".blue()
        )
        locales.addMessage(
            Locales.ENGLISH,
            MessageKeys.HELP_PAGE_INFORMATION,
            "Page {page} out of {totalpages} pages".blue()
        )
        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_NO_RESULTS, "No results were found!".red())
        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_SEARCH_HEADER, "Results for &f{search}".blue())


        locales.addMessage(Locales.ENGLISH, MessageKeys.HELP_DETAILED_HEADER, "$prefix &fCommand Help for &e/{command}")
        locales.addMessage(
            Locales.ENGLISH,
            MessageKeys.HELP_DETAILED_COMMAND_FORMAT,
            "Usage: &f/{command} {parameters}".blue()
        )

        locales.addMessage(
            Locales.ENGLISH,
            MessageKeys.HELP_DETAILED_PARAMETER_FORMAT,
            "&7- &f{parameter} &7({description})"
        )

        locales.addMessage(Locales.ENGLISH, MessageKeys.INVALID_SYNTAX, "$prefix" + "Use &e{command} &f{syntax}".blue())
        locales.addMessage(
            Locales.ENGLISH,
            MessageKeys.PERMISSION_DENIED,
            "$prefix" + "You don't have permission to use this command!".red()
        )
        locales.addMessage(
            Locales.ENGLISH,
            MessageKeys.UNKNOWN_COMMAND,
            "$prefix" + "That command does not exist!".red()
        )

        ConfirmCondition.register(commandManager)
        CooldownCondition.register(commandManager)

        registerTargetSelectors()

        commandManager.getCommandContexts().registerContext(Enchantment::class.java, { context ->
            val enchantment = Enchantment.getByName(context.popFirstArg())
            if (enchantment == null) throw InvalidCommandArgument("Enchantment not found", false)
            enchantment
        })


        commandManager.getCommandContexts().registerContext(InvestigationPriority::class.java, { context ->
            val priority = InvestigationPriority.valueOf(context.popFirstArg())
            if (priority == null) throw InvalidCommandArgument("Priority not found", false)
            priority
        })

        commandManager.commandContexts.registerContext(Attribute::class.java) { context ->
            val arg = context.popFirstArg()
            try {
                Attribute.valueOf(arg.uppercase())
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException("Attribute '$arg' does not exist!")
            }
        }

        commandManager.enableUnstableAPI("help")

    }


    fun registerCompletions() {
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

        if (coreConfig.features.warps) {
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

        commandManager.commandCompletions.registerCompletion("worlds") { context ->
            Bukkit.getWorlds().map { it.name }
        }


        commandManager.commandCompletions.registerCompletion("homes") { context ->
            val sender = context.sender
            if (sender !is Player) return@registerCompletion emptyList()

            val user = UserManager.get(sender)

            if (user == null) {
                return@registerCompletion emptyList()
            }

            user.homes.map { it.name }
        }

        commandManager.commandCompletions.registerCompletion("playerhomes") { context ->
            val targetName = context.input
            if (targetName.isBlank()) return@registerCompletion emptyList()

            val target = Bukkit.getOfflinePlayerIfCached(targetName) ?: return@registerCompletion emptyList()
            val user = UserManager.getIfLoaded(target.uniqueId) ?: return@registerCompletion emptyList()
            user.homes.map { it.name }
        }

        commandManager.commandCompletions.registerCompletion("materials") { context ->
            Material.entries
                .filter { !it.isLegacy }
                .map { it.name }
                .toList()
        }

        commandManager.commandCompletions.registerCompletion("predefinedBroadcasts") { context ->
            coreConfig.broadcastConfig.predefinedBroadcasts.keys.toList()
        }

        commandManager.commandCompletions.registerCompletion("colors") { context ->
            coreConfig.chat.chatColor.colors.keys.toList()
        }

        commandManager.commandCompletions.registerCompletion("enchantsWithLevels") { ctx ->
            val input = ctx.input
            val available = Enchantment.values().map { it.key.key.lowercase() }


            if (input.contains("|")) {
                val parts = input.split("|")
                val enchName = parts[0].lowercase()
                val ench = Enchantment.getByName(enchName.uppercase())
                if (ench != null) {
                    val max = ench.maxLevel.coerceAtLeast(1)
                    return@registerCompletion (1..max).map { "$enchName|$it" }
                }
                return@registerCompletion emptyList<String>()
            }


            available.filter { it.startsWith(input.lowercase()) }
        }


        commandManager.commandCompletions.registerCompletion("punishtypes") { _ ->
            PunishmentType.values().map { it.displayName }
        }

        commandManager.commandCompletions.registerCompletion("reasons") { context ->
            PunishmentService.config.reasons.categories
                .flatMap { it.value.keys }
                .toList()
        }


        commandManager.commandCompletions.registerCompletion("times") { context ->
            val input = context.input.trim()
            val base = input.toIntOrNull()

            if (base != null) {
                return@registerCompletion listOf("${base}s", "${base}m", "${base}h", "${base}d")
            }

            listOf("10s", "30s", "1m", "5m", "10m", "30m", "1h", "1d", "7d")
        }

        commandManager.commandCompletions.registerCompletion("logtypes") { context ->
            LogType.values().map { it.name }
        }


        commandManager.commandCompletions.registerCompletion("removalReasons") { context ->
            PunishmentService.config.removalReasons.reasons.toList()
        }


        commandManager.registerItemCompletions()
    }

    fun registerTargetSelectors() {
        resolver = TargetSelectorResolver()

        commandManager.commandContexts.registerContext(
            TargetPlayers::class.java,
            PlayerOnlyResolver()
        )

        commandManager.commandContexts.registerContext(
            TargetEntities::class.java,
            AnyTargetResolver()
        )

        commandManager.commandContexts.registerContext(
            TargetSinglePlayer::class.java,
            SinglePlayerResolver()
        )


        commandManager.commandContexts.registerContext(
            Collection::class.java as Class<Collection<Entity>>
        ) { ctx ->
            TargetSelectorResolver(TargetType.ALL).getContext(ctx)
        }

        commandManager.commandCompletions.registerCompletion("targets") { context ->
            val input = context.input ?: ""
            resolver.getTabCompletions(input)
        }
    }

    private fun getClassesInPackage(packageName: String): List<Class<*>> {
        val path = packageName.replace('.', '/')
        val classLoader = javaClass.classLoader as URLClassLoader
        val classes = mutableListOf<Class<*>>()

        classLoader.urLs.forEach { url ->
            if (url.path.endsWith(".jar")) {
                val jarFile = JarFile(url.path)
                jarFile.entries().asSequence().forEach { entry ->
                    if (entry.name.endsWith(".class") && entry.name.startsWith(path)) {
                        val className = entry.name.removeSuffix(".class").replace('/', '.')
                        try {
                            val clazz = Class.forName(className)
                            if (clazz.getAnnotation(Setting::class.java) != null) {
                                classes.add(clazz)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }

        return classes
    }

}
