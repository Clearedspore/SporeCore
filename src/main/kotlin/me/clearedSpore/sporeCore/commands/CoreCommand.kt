package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.io.File
import java.util.concurrent.CompletableFuture

@CommandAlias("sporecore|sc")
class CoreCommand : BaseCommand() {

    private val startTime = System.currentTimeMillis()

    @Subcommand("reload")
    @CommandPermission(Perm.ADMIN)
    fun onReload(sender: CommandSender) {
        val plugin = SporeCore.instance
        sender.sendMessage("Reloading SporeCore asynchronously...".blue())

        val startTime = System.currentTimeMillis()

        reloadConfig(plugin, sender)
            .thenCompose { reloadEconomy() }
            .thenCompose { reloadWarps(plugin) }
            .thenCompose { reloadDatabase(plugin) }
            .thenRun {
                val duration = System.currentTimeMillis() - startTime
                sender.sendMessage("Reload complete. Took ${duration}ms.".blue())
                Logger.info("SporeCore reloaded in ${duration}ms.")
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

    private fun reloadDatabase(plugin: SporeCore): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            Logger.infoDB("Reloading database...")
            DatabaseManager.close()
            DatabaseManager.init(plugin.dataFolder)
            plugin.database = me.clearedSpore.sporeCore.database.Database()
            Logger.infoDB("Database reloaded successfully.")
        }
    }


    @Subcommand("version")
    fun onVersion(sender: CommandSender) {
        val desc = SporeCore.instance.description
        sender.sendMessage(desc.name.blue() + " v${desc.version}".green())
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
            sender.userFail()
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
            sender.userFail()
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

        val user = UserManager.get(target) ?: run {
            sender.userFail()
            return
        }

        sender.sendMessage("=== User Info for ${target.name} ===".blue())
        sender.sendMessage("UUID: ".white() + target.uniqueId.toString().green())
        sender.sendMessage("First Join: ".white() + (user.firstJoin ?: "Unknown").green())
        sender.sendMessage("Homes: ".white() + user.homes.size.toString().green())
        sender.sendMessage("Pending Messages: ".white() + user.pendingMessages.size.toString().green())
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
