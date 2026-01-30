package me.clearedSpore.sporeCore.commands.moderation.mode

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.features.mode.`object`.Mode
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

@CommandAlias("%modealias")
@CommandPermission(Perm.MODE_ALLOW)
class CustomModeCommand(
    val mode: Mode,
) : BaseCommand() {

    @Default
    fun onModeToggle(sender: CommandSender, @Optional @Name("target") target: OnlinePlayer?) {
        if (target == null && sender is ConsoleCommandSender) {
            sender.sendMessage("Console must provide a target!")
            return
        }

        val service = ModeService
        if (service.getModeById(mode.id) == null) {
            sender.sendMessage("Failed to apply mode!".red())
            return
        }

        if (target == null) {
            val player = Bukkit.getPlayer(sender.name)
            if (player == null) {
                sender.userFail()
                return
            }

            if (!player.hasPermission(mode.permission)) {
                player.sendErrorMessage("You don't have permission to toggle this mode!")
                return
            }

            val enabled = service.isInMode(player)
            val status = if (enabled == true) "Disabled" else "Enabled"

            service.toggleMode(player, mode.id)
            sender.sendMessage("$status ${mode.name} mode".blue())
            Logger.log(sender, Perm.LOG, "$status ${mode.name}", false)
        } else {

            if (!sender.hasPermission(Perm.MODE_OTHERS) && !sender.hasPermission(mode.permission)) {
                sender.sendMessage("You don't have permission to toggle that mode for another player!".red())
                return
            }

            val enabled = service.isInMode(target.player)
            val status = if (enabled == true) "Disabled" else "Enabled"

            service.toggleMode(target.player, mode.id)
            sender.sendMessage("$status ${mode.name} mode for ${target.player.name}".blue())
            Logger.log(sender, Perm.LOG, "$status ${mode.name} mode for ${target.player.name}", false)
        }
    }
}