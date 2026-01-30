package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.orange
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player

@CommandAlias("ping")
@CommandPermission(Perm.PING)
@SporeCoreCommand
class PingCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    @Syntax("[player]")
    fun onPing(sender: Player, @Optional onlineTarget: OnlinePlayer?) {
        val target = onlineTarget?.player ?: sender
        val ping = target.ping

        val formattedPing = when {
            ping < 90 -> "$ping".green()
            ping < 120 -> "&e$ping"
            ping < 150 -> "$ping".orange()
            else -> "$ping".red()
        }

        if (target == sender) {
            sender.sendMessage("Your ping is $formattedPing".blue())
        } else {

            if (!sender.hasPermission(Perm.PING_OTHERS)) {
                sender.sendErrorMessage("You don't have permission to check other players their ping!".red())
                return
            }

            sender.sendMessage("${target.name}'s ping is $formattedPing".blue())
        }
    }
}