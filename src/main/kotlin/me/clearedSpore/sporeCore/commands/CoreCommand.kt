package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.currency.CurrencySystemService
import me.clearedSpore.sporeCore.features.currency.`object`.CreditAction
import me.clearedSpore.sporeCore.features.currency.`object`.CreditLog
import me.clearedSpore.sporeCore.features.currency.`object`.PackagePurchase
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.config.PunishmentLogConfig
import me.clearedSpore.sporeCore.features.stats.StatService
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

@CommandAlias("sporecore|core")
class CoreCommand : BaseCommand() {

    private val startTime = System.currentTimeMillis()

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

                if(plugin.coreConfig.features.punishments){
                    PunishmentService.load()
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
    fun onCheck(sender: CommandSender){
        val plugin = SporeCore.instance
        val checker = plugin.updateChecker
        checker.checkForUpdates()


        if(checker.updateAvailable){
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


    @Subcommand("dumpdb")
    @CommandPermission(Perm.ADMIN)
    fun onDumpDB(sender: CommandSender) {
        sender.sendMessage("Dumping database to JSON...".blue())

        CompletableFuture.runAsync {
            try {
                val plugin = SporeCore.instance
                val file = File(plugin.dataFolder, "database_dump.json")

                val userCollection = DatabaseManager.getUserCollection()
                val serverCollection = DatabaseManager.getServerCollection()

                val dump: MutableMap<String, List<Map<String, Any>>> = mutableMapOf()

                dump["users"] = userCollection.find().map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    (doc as Map<String, Any>).mapValues { it.value!! }
                }.toList()

                dump["serverData"] = serverCollection.find().map { doc ->
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


    @Subcommand("generateusers")
    @CommandPermission("*")
    @Private
    fun onGenerate(sender: CommandSender, amountArg: Int?) {

        if(sender is Player){
            sender.sendMessage("You can only run this command trough console!".red())
            return
        }

        val amount = (amountArg ?: 500).coerceIn(1, 5000)
        sender.sendMessage("Starting generation of $amount fake users...".blue())

        CompletableFuture.runAsync {
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

                    user.creditLogs.add(0, CreditLog(CreditAction.ADDED, purchased, "Test purchase", now - Random.nextLong(0, sixtyDaysMs)))

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


    @Subcommand("user info")
    @CommandPermission(Perm.ADMIN)
    @Syntax("<player>")
    @CommandCompletion("@players")
    fun onUserInfo(sender: CommandSender, playerName: String) {
        val target = Bukkit.getOfflinePlayer(playerName)

        val user = UserManager.get(target.uniqueId)

        if(user == null){
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
        sender.sendMessage("Credits Spent: ".white() + user.creditsSpent.toString().gray() + " (creditsSpent)".gray())
        sender.sendMessage("Last Location: ".white() + (user.lastLocation?.let { loc ->
            "X: ${"%.1f".format(loc.x)}, Y: ${"%.1f".format(loc.y)}, Z: ${"%.1f".format(loc.z)}, World: ${loc.world?.name}"
        } ?: "None").green() + " (lastLocation)".gray())
        sender.sendMessage("Pending Payments: ".white() + " (pendingPayments)".gray())
        if(user.pendingPayments.isNotEmpty()) {
            user.pendingPayments.forEach { (senderName, total) ->
                val formattedAmount = EconomyService.format(total)
                sender.sendMessage("   ${formattedAmount.green()} from ${senderName.white()}".blue())
            }
        } else {
            sender.sendMessage("   User has no pending payments".red())
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
