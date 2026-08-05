package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

@CommandAlias("vanish|v")
@CommandPermission(Perm.VANISH)
class VanishCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    fun onVanish(sender: CommandSender, @Optional @Name("target") target: OnlinePlayer?) {
        var suffix = if (sender is Player) chatService?.getPlayerSuffix(sender)?.translate() ?: "" else ""
        var targetSuffix = if (target != null) chatService?.getPlayerSuffix(target.player)?.translate() ?: "" else " "
        if (sender is ConsoleCommandSender && target == null) {
            sender.sendMessage("Console must support a target!".red())
            return
        }

        if (target == null) {
            val player = Bukkit.getPlayer(sender.name)
            if (player == null) {
                return
            }

            VanishService.toggle(player.uniqueId)
            val isVanished = VanishService.isVanished(player.uniqueId)
            player.sendMessage(if (isVanished) "Enabled Vanish".blue() else "Disabled Vanish".red())
            Logger.log(suffix, sender, Perm.LOG, if (isVanished) "enabled vanish" else "disabled vanish", false)
        } else {
            if (!sender.hasPermission(Perm.VANISH_OTHERS)) {
                sender.sendMessage("You don't have permission to toggle other players their vanish!".red())
                return
            }

            VanishService.toggle(target.player.uniqueId)
            val isVanished = VanishService.isVanished(target.player.uniqueId)
            sender.sendMessage(if (isVanished) "Enabled Vanish for ${targetSuffix}${target.player.name}".blue() else "Disabled Vanish for ${targetSuffix}${target.player.name}".red())
            Logger.log(
                suffix,
                sender,
                Perm.LOG,
                if (isVanished) "enabled vanish for ${targetSuffix}${target.player.name}" else "disabled vanish for ${targetSuffix}${target.player.name}",
                false
            )
        }
    }
}