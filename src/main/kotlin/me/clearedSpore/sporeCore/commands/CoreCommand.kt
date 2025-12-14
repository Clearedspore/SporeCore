package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import co.aikar.commands.annotation.Optional
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.currency.CurrencySystemService
import me.clearedSpore.sporeCore.features.currency.`object`.CreditAction
import me.clearedSpore.sporeCore.features.currency.`object`.CreditLog
import me.clearedSpore.sporeCore.features.currency.`object`.PackagePurchase
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.stats.StatService
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.menu.rollback.StaffRollbackPreviewMenu
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Tasks
import me.clearedSpore.sporeCore.util.button.CallbackRegistry
import me.clearedSpore.sporeCore.util.button.TextButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.random.Random

@CommandAlias("sporecore|core")
class CoreCommand : BaseCommand() {

    private val startTime = System.currentTimeMillis()

    @Default()
    fun onHelp(help: CommandHelp){
        help.showHelp()
    }

    @Subcommand("reload")
    @CommandPermission(Perm.ADMIN)
    fun onReload(sender: CommandSender) {
        val plugin = SporeCore.instance
        sender.sendMessage("Reloading SporeCore asynchronously...".blue())

        val startTime = System.currentTimeMillis()
        Logger.initialize(plugin.coreConfig.general.prefix)
        Message.init(true)
        reloadConfig(plugin, sender)
            .thenCompose { reloadEconomy() }
            .thenCompose { reloadWarps(plugin) }
            .thenCompose { reloadKits(plugin) }
            .thenCompose { reloadDatabase() }
            .thenRun {
                if (plugin.coreConfig.features.currency.enabled) {
                    CurrencySystemService.reload()
                }

                if (plugin.coreConfig.features.punishments) {
                    PunishmentService.load()
                }

                if(plugin.coreConfig.features.modes){
                    ModeService.initialize()
                }

                val duration = System.currentTimeMillis() - startTime
                sender.sendMessage("Reload complete. Took ${duration}ms.".blue())
                Logger.info("SporeCore reloaded in ${duration}ms, by ${sender.name}.")
                Logger.log(sender, Perm.ADMIN_LOG, "reloaded the plugin", false)
            }

            .exceptionally { ex ->
                sender.sendMessage("Reload failed: ${ex.message}".red())
                Logger.error("Reload error: ${ex.message}")
                ex.printStackTrace()
                null
            }
    }


    private fun reloadConfig(plugin: SporeCore, sender: CommandSender): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            Logger.info("Reloading config...")
            try {
                val configFile = File(plugin.dataFolder, "config.yml").toPath()
                plugin.coreConfig = YamlConfigurations.update(configFile, CoreConfig::class.java)
                Logger.initialize(plugin.coreConfig.general.prefix)
                Logger.info("Config reloaded successfully.")

                sender.sendMessage("Config reloaded successfully!".blue())
            } catch (ex: ConfigurationException) {
                val shortMessage = ex.message?.lineSequence()?.firstOrNull() ?: "Invalid configuration!"
                sender.sendMessage("Failed to reload config: $shortMessage".red())

                Logger.error("Failed to reload config.yml!")
                Logger.error("Reason: $shortMessage")
                ex.printStackTrace()
            }
        }
    }


    private fun reloadEconomy(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            Logger.info("Reloading Vault economy...")
            try {
                EconomyService.reloadAsync()
            } catch (ex: Exception) {
                Logger.error("Failed to reload EconomyService: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }


    private fun reloadWarps(plugin: SporeCore): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (plugin.coreConfig.features.warps) {
                Logger.info("Reloading warps")
                plugin.warpService.reloadWarps()
                Logger.info("Warps reloaded successfully.")
            } else {
                Logger.warn("Warps are disabled in config.")
            }
        }
    }


    private fun reloadKits(plugin: SporeCore): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (plugin.coreConfig.features.kits) {
                Logger.info("Reloading kits")
                plugin.kitService.reloadKits()
                Logger.info("Kits reloaded successfully.")
            } else {
                Logger.warn("Kits are disabled in config.")
            }
        }
    }

    private fun reloadDatabase(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            Logger.infoDB("Reloading database...")
            DatabaseManager.saveServerData()
            Logger.infoDB("Database reloaded successfully.")
        }
    }

    @Subcommand("checkupdate")
    @CommandPermission(Perm.UPDATECHEKER)
    fun onCheck(sender: CommandSender) {
        val plugin = SporeCore.instance
        val checker = plugin.updateChecker
        checker.checkForUpdates()


        if (checker.updateAvailable) {
            sender.sendMessage("[SporeCore] &fA new update is available!".blue())
            sender.sendMessage("[SporeCore] &fCurrent version: &e${plugin.description.version}".blue())
            sender.sendMessage("[SporeCore] &fLatest version: &e${checker.latestVersion}".blue())
            sender.sendMessage("[SporeCore] &fDownload at: &ehttps://www.spigotmc.org/resources/sporecore.121185/".blue())
        } else {
            sender.sendMessage("[SporeCore] Plugin is up to date!".blue())
        }
    }

    @Subcommand("version")
    fun onVersion(sender: CommandSender) {
        val desc = SporeCore.instance.description
        sender.sendMessage("Version: ".white() + " v${desc.version}".green())
        sender.sendMessage("Author(s): ".white() + desc.authors.joinToString(", ").blue())
    }

    @Subcommand("info")
    fun onInfo(sender: CommandSender) {
        val uptime = System.currentTimeMillis() - startTime
        val uptimeFormatted = formatDuration(uptime)
        val desc = SporeCore.instance.description

        sender.sendMessage("=== Plugin Info ===".blue())
        sender.sendMessage("Name: ".white() + desc.name.green())
        sender.sendMessage("Version: ".white() + desc.version.green())
        sender.sendMessage("Authors: ".white() + desc.authors.joinToString(", ").green())
        sender.sendMessage("Uptime: ".white() + uptimeFormatted.green())
        sender.sendMessage("Loaded Config: ".white() + (if (SporeCore.instance.coreConfig != null) "✅" else "❌"))
    }


    @Subcommand("user get")
    @CommandPermission(Perm.ADMIN)
    @Syntax("<player> <field>")
    @CommandCompletion("@players @nothing")
    fun onUserGet(sender: CommandSender, playerName: String, fieldName: String) {
        val target = Bukkit.getOfflinePlayer(playerName)

        if (!target.hasPlayedBefore() && !target.isOnline) {
            sender.sendMessage("That player has never joined before!".red())
            return
        }

        val user = UserManager.get(target) ?: run {
            sender.userJoinFail()
            return
        }

        val field = user::class.java.declaredFields.find { it.name == fieldName }
        if (field == null) {
            sender.sendMessage("Field '$fieldName' not found in user data.".red())
            return
        }

        field.isAccessible = true
        val value = field.get(user)
        sender.sendMessage("Value of '$fieldName' for ${target.name}: ".blue() + value.toString().green())
    }

    @Subcommand("staffrollback")
    @CommandPermission("*")
    @Syntax("<staff> <time>")
    @CommandCompletion("@players @times")
    fun onStaffRollback(sender: CommandSender, staffName: String, timeArg: String, @Optional confirm: String?) {
        val start = System.currentTimeMillis()
        sender.sendMessage("Processing...".blue())

        Tasks.runAsync {
            val staffPlayer = Bukkit.getOfflinePlayers()
                .firstOrNull { it.name.equals(staffName, ignoreCase = true) }

            if (staffPlayer == null) {
                sender.sendMessage("Staff member '$staffName' not found.".red())
                return@runAsync
            }

            val millisBack = TimeUtil.parseDuration(timeArg)
            if (millisBack <= 0) {
                sender.sendMessage("Invalid time: '$timeArg'.".red())
                return@runAsync
            }

            val cutoff = System.currentTimeMillis() - millisBack

            val staffUser = UserManager.get(staffPlayer.uniqueId)
            if (staffUser == null) {
                sender.sendMessage("Staff member not found in database.".red())
                return@runAsync
            }

            val statsToRollback = staffUser.staffStats
                .filter { it.date.time >= cutoff }
                .filter { it.type in setOf(
                    PunishmentType.BAN, PunishmentType.TEMPBAN,
                    PunishmentType.MUTE, PunishmentType.TEMPMUTE,
                    PunishmentType.WARN, PunishmentType.TEMPWARN
                ) }
                .sortedByDescending { it.date.time }

            if (statsToRollback.isEmpty()) {
                sender.sendMessage("No punishments by $staffName within $timeArg.".red())
                return@runAsync
            }

            if (confirm == null || confirm != "confirm") {
                if (sender is Player) {
                    val viewer = sender

                    Tasks.runSync {
                        if (statsToRollback.isEmpty()) {
                            sender.sendMessage("No punishments by $staffName within $timeArg.".red())
                            return@runSync
                        }
                        val menu = StaffRollbackPreviewMenu(viewer, staffPlayer, timeArg, statsToRollback)
                        menu.open(viewer)
                    }

                    return@runAsync
                } else {
                    sender.sendMessage("Console must use confirm to execute rollbacks.".red())
                    sender.sendMessage("Run /sporecore staffrollback $staffName $timeArg confirm".blue())
                    sender.sendMessage("To confirm".blue())
                    return@runAsync
                }
            }

            val senderUser: User = if (sender is Player) {
                UserManager.get(sender) ?: run {
                    sender.userFail()
                    return@runAsync
                }
            } else {
                UserManager.getConsoleUser()
            }

            var rollbackCount = 0
            statsToRollback.forEach { stat ->
                val targetUser = UserManager.get(stat.targetUuid) ?: return@forEach

                val punishment = targetUser.punishments.firstOrNull { it.id == stat.punishmentId } ?: return@forEach

                val removed = when (punishment.type) {
                    PunishmentType.BAN, PunishmentType.TEMPBAN ->
                        targetUser.unban(UserManager.getConsoleUser(), punishment.id, "Staff rollback - Issued by ${senderUser.playerName}")
                    PunishmentType.MUTE, PunishmentType.TEMPMUTE ->
                        targetUser.unmute(UserManager.getConsoleUser(), punishment.id, "Staff rollback - Issued by ${senderUser.playerName}")
                    PunishmentType.WARN, PunishmentType.TEMPWARN ->
                        targetUser.unwarn(UserManager.getConsoleUser(), punishment.id, "Staff rollback - Issued by ${senderUser.playerName}")
                    else -> false
                }

                if (removed) rollbackCount++
                UserManager.save(targetUser)
            }

            val end = System.currentTimeMillis()
            sender.sendMessage(
                "Rolled back $rollbackCount punishments from $staffName in the last $timeArg (took ${end - start}ms).".blue()
            )
            Logger.log(sender, Perm.ADMIN_LOG, "rolled back punishments made by $staffName", true)
            val config = SporeCore.instance.coreConfig.discord
            val webhook = Webhook(config.staffRollback)
            if(config.staffRollbackPing.isNullOrBlank()) {
                webhook.setMessage("${sender.name} has rolled back $rollbackCount punishments from $staffName in the last $timeArg")
            } else {
                val ping = config.staffRollbackPing
                webhook.setMessage(
                    "$ping ${sender.name} has rolled back $rollbackCount punishments from $staffName in the last $timeArg"
                )
            }
                webhook.setUsername("SporeCore Logs")
                    .setProfileURL("https://cdn.modrinth.com/data/8X4HqUuD/980c64224cb4fb48829d90a0d51c36b565ad8a05_96.webp")

            webhook.send()

        }
    }


    @Subcommand("dumpdb")
    @CommandPermission(Perm.ADMIN)
    fun onDumpDB(sender: CommandSender) {
        sender.sendMessage("Dumping database to JSON...".blue())

        Tasks.runAsync {
            try {
                val plugin = SporeCore.instance
                val file = File(plugin.dataFolder, "database_dump.json")

                val userCollection = DatabaseManager.getUserCollection()
                val serverCollection = DatabaseManager.getServerCollection()
                val inventoryCollection = DatabaseManager.getInventoryCollection()

                val dump: MutableMap<String, List<Map<String, Any>>> = mutableMapOf()

                dump["users"] = userCollection.find().map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    (doc as Map<String, Any>).mapValues { it.value!! }
                }.toList()

                dump["serverData"] = serverCollection.find().map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    (doc as Map<String, Any>).mapValues { it.value!! }
                }.toList()

                dump["inventories"] = inventoryCollection.find().map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    (doc as Map<String, Any>).mapValues { it.value!! }
                }.toList()

                val json = com.google.gson.GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(dump)

                file.writeText(json)

                sender.sendMessage("Database successfully dumped to ${file.name}".green())
                Logger.infoDB("Database dump saved to ${file.absolutePath}")
            } catch (ex: Exception) {
                sender.sendMessage("Failed to dump database: ${ex.message}".red())
                Logger.error("Database dump failed: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    @Subcommand("wiki")
    @CommandCompletion("currency|punishments|channels|modes")
    fun onWiki(sender: CommandSender, @Optional @Name("feature") feature: String?) {

        val base = "https://spore-plugins.gitbook.io/sporecore/"

        val pages = mapOf(
            "currency" to "${base}custom-currency",
            "punishments" to "${base}punishments",
            "channels" to "${base}channels",
            "modes" to "${base}features/modes"
        )

        if (feature.isNullOrEmpty()) {
            sender.sendMessage("Wiki&7:&f $base".blue())
            return
        }

        val key = feature.lowercase()
        val url = pages[key]

        if (url == null) {
            sender.sendMessage("Unknown wiki page. Use: currency, punishments, channels, modes".red())
            return
        }

        sender.sendMessage("Wiki for &e$key&7: &f$url".blue())
    }

    @Subcommand("simulate-lag")
    @CommandPermission("*")
    fun simulatelag(sender: CommandSender, ms: Long) {
        sender.sendMessage("Server will now lag for " + ms + " milliseconds!".blue())

        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            sender.sendRichMessage("Interrupted!".red())
        }
    }

    @Subcommand("generateusers")
    @CommandPermission("*")
    @Private
    fun onGenerate(sender: CommandSender, amountArg: Int?) {

        if (sender is Player) {
            sender.sendMessage("You can only run this command trough console!".red())
            return
        }

        val amount = (amountArg ?: 500).coerceIn(1, 5000)
        sender.sendMessage("Starting generation of $amount fake users...".blue())

        Tasks.runAsync {
            try {
                val databaseCollection = DatabaseManager.getServerCollection()
                val database = DatabaseManager.getServerData()

                val userCollection = DatabaseManager.getUserCollection()

                val packageNames = CurrencySystemService.getCategories().values
                    .flatMap { it.items.values.mapNotNull { si -> si.name } }
                    .ifEmpty { listOf("TestPackage", "StarterPack", "Rank-Upgrade", "KeysBundle") }

                var created = 0
                val now = System.currentTimeMillis()
                val sixtyDaysMs = 60L * 24 * 60 * 60 * 1000

                repeat(amount) {
                    val uuid = UUID.randomUUID()
                    val name = "TestUser${Random.nextInt(100_000, 999_999)}"

                    val user = User.create(uuid, name, userCollection)

                    val purchased = Random.nextInt(1, 1001).toDouble()

                    val totalSpent = Random.nextInt(0, purchased.toInt() + 1).toDouble()
                    val spentEntriesCount = if (totalSpent <= 0.0) 0 else Random.nextInt(1, 5)
                    val spentSplits = mutableListOf<Double>()
                    var remaining = totalSpent
                    repeat(spentEntriesCount) { idx ->
                        val remainingSlots = spentEntriesCount - idx
                        val take = if (remainingSlots == 1) remaining else {
                            val maxTake = (remaining / remainingSlots * 2).coerceAtLeast(1.0)
                            Random.nextDouble(1.0, maxTake.coerceAtMost(remaining))
                        }
                        spentSplits.add(kotlin.math.min(take, remaining))
                        remaining -= spentSplits.last()
                    }

                    user.creditLogs.add(
                        0,
                        CreditLog(CreditAction.ADDED, purchased, "Test purchase", now - Random.nextLong(0, sixtyDaysMs))
                    )

                    var spentSoFar = 0.0
                    for (s in spentSplits) {
                        val timestamp = now - Random.nextLong(0, sixtyDaysMs)
                        user.creditLogs.add(0, CreditLog(CreditAction.SPENT, s, "Test spend", timestamp))
                        spentSoFar += s


                        if (Random.nextBoolean()) {
                            val timestamp = System.currentTimeMillis()
                            val pkgName = packageNames.random()
                            val amount = Random.nextInt(1, 1000)

                            val purchase = PackagePurchase(pkgName, amount, timestamp)
                            database.packagePurchases.add(purchase)
                        }

                    }

                    user.credits = (purchased - spentSoFar).coerceAtLeast(0.0)
                    user.balance = Random.nextInt(0, 2000).toDouble()

                    UserManager.save(user)
                    created++
                }

                database.save(databaseCollection)

                sender.sendMessage("Generation complete: created $created fake users.".blue())
                Logger.info("Currency test data: created $created fake users for load testing.")
            } catch (ex: Exception) {
                sender.sendMessage("Failed generating test data: ${ex.message}".red())
                Logger.warn("Failed generating test data: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }


    @Subcommand("user set")
    @CommandPermission(Perm.ADMIN)
    @Syntax("<player> <field> <value>")
    @CommandCompletion("@players @nothing")
    fun onUserSet(sender: CommandSender, playerName: String, fieldName: String, value: String) {
        val target = Bukkit.getOfflinePlayer(playerName)

        if (!target.hasPlayedBefore() && !target.isOnline) {
            sender.sendMessage("That player has never joined before!".red())
            return
        }

        val user = UserManager.get(target) ?: run {
            sender.userJoinFail()
            return
        }

        val field = user::class.java.declaredFields.find { it.name == fieldName }
        if (field == null) {
            sender.sendMessage("Field '$fieldName' not found in user data.".red())
            return
        }

        try {
            field.isAccessible = true
            val castValue = when (field.type) {
                Boolean::class.java -> value.toBoolean()
                Int::class.java -> value.toInt()
                Double::class.java -> value.toDouble()
                Float::class.java -> value.toFloat()
                else -> value
            }
            field.set(user, castValue)
            UserManager.save(user)
            sender.sendMessage("Set '$fieldName' for ${target.name} to $value.".blue())
        } catch (ex: Exception) {
            sender.sendMessage("Failed to set value: ${ex.message}".red())
            ex.printStackTrace()
        }
    }

    @Subcommand("callback")
    @Syntax("<id>")
    fun onCallback(sender: CommandSender, id: String) {
        CallbackRegistry.execute(sender, id)
    }


    @Subcommand("user info")
    @CommandPermission(Perm.ADMIN)
    @Syntax("<player>")
    @CommandCompletion("@players")
    fun onUserInfo(sender: CommandSender, playerName: String) {
        val target = Bukkit.getOfflinePlayer(playerName)
        val user = UserManager.get(target.uniqueId)

        if (user == null) {
            sender.userJoinFail()
            return
        }

        sender.sendMessage("=== User Info for ${target.name} ===".blue())
        sender.sendMessage("UUID: ".white() + target.uniqueId.toString().green() + " (uuidStr)".gray())
        sender.sendMessage("First Join: ".white() + (user.firstJoin ?: "Unknown").green() + " (firstJoin)".gray())
        sender.sendMessage("Homes: ".white() + user.homes.size.toString().green() + " (homes)".gray())
        sender.sendMessage("Pending Messages: ".white() + user.pendingMessages.size.toString().green() + " (pendingMessages)".gray())
        sender.sendMessage("Balance: ".white() + EconomyService.format(user.balance).green() + " (balance)".gray())
        sender.sendMessage("Last join: ".white() + user.lastJoin?.green() + " (lastJoin)".gray())
        sender.sendMessage("Playtime: ".white() + StatService.getTotalPlaytime(user).toString().green() + " (totalPlaytime)".gray())
        sender.sendMessage("Credits: ".white() + user.credits.toString().gray() + " (credits)".gray())
        sender.sendMessage("Credits Spent: ".white() + user.creditsSpent + " (creditsSpent)".gray())

        sender.sendMessage(
            "Last Location: ".white() +
                    (user.lastLocation?.let { loc ->
                        "X: %.1f, Y: %.1f, Z: %.1f, World: ${loc.world?.name}"
                            .format(loc.x, loc.y, loc.z)
                    } ?: "None").green() + " (lastLocation)".gray()
        )

        sender.sendMessage("Pending Payments: ".white() + "(pendingPayments)".gray())
        if (user.pendingPayments.isNotEmpty()) {
            user.pendingPayments.forEach { (senderName, total) ->
                sender.sendMessage("   ${EconomyService.format(total).green()} from ${senderName.white()}".blue())
            }
        } else {
            sender.sendMessage("   User has no pending payments".red())
        }

        sender.sendMessage("Channel: ".white() + (user.channel ?: "Public").green() + " (channel)".gray())

        sender.sendMessage("Pending inventories: ".white() + "(pendingInventories)".gray())

        if (user.pendingInventories.isNotEmpty()) {
            val button = TextButton("[Click to view]".blue())
                .hoverEvent("Click to view all inventories")
                .onClick { s ->
                    displayPendingInventories(s, user)
                }
                .build(sender)

            sender.sendMessage(button)
        } else {
            sender.sendMessage("   User has no pending inventories".red())
        }
    }


    fun displayPendingInventories(sender: CommandSender, user: User) {
        if (user.pendingInventories.isEmpty()) {
            sender.sendMessage("   User has no pending inventories".red())
            return
        }

        user.pendingInventories.forEachIndexed { index, id ->
            val inventory = InventoryManager.getInventory(id)

            if(inventory == null){
                sender.sendErrorMessage("Failed to load inventory!")
                return
            }

            sender.sendMessage("${index + 1}:")
            sender.sendMessage("  ID: ".white() + "${inventory.id}\n".blue())
            sender.sendMessage("  Timestamp: ".white() + "${inventory.formattedAge()}\n".blue())
            sender.sendMessage("  Store reason: ".white() + " ${inventory.storeReason}\n".blue())
            sender.sendMessage("  Rollback issuer: ".white() + "${inventory.rollbackIssuer}\n".blue())
            sender.sendMessage("${index + 1}:")
            val button = TextButton("  [Click to remove]".red())
                .onClick {
                    try {
                        user.pendingInventories.remove(inventory.id)
                        sender.sendMessage("Successfully removed the inventory.".blue())
                        UserManager.save(user)
                    } catch (e: Exception){
                        sender.sendMessage("Failed to delete inventory!".red())
                    }
                }
                .hoverEvent("Click to delete")
                .build(sender)

            sender.sendMessage(button)
        }
    }

    private fun formatDuration(ms: Long): String {
        val seconds = ms / 1000 % 60
        val minutes = ms / (1000 * 60) % 60
        val hours = ms / (1000 * 60 * 60) % 24
        val days = ms / (1000 * 60 * 60 * 24)

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            append("${seconds}s")
        }.trim()
    }

}
