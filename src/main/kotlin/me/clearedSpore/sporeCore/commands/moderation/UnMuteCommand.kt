package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

@CommandAlias("unmute")
@CommandPermission(Perm.UNMUTE)
class UnMuteCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players @removalReasons")
    @Syntax("<player> <reason>")
    fun onUnmute(sender: CommandSender, targetName: String, reason: String) {
        val target = Bukkit.getOfflinePlayer(targetName)
        val targetUser = UserManager.get(target) ?: run {
            sender.userJoinFail()
            return
        }

        val senderUser: User = when (sender) {
            is Player -> UserManager.get(sender) ?: run { sender.userFail(); return }
            is ConsoleCommandSender -> UserManager.getConsoleUser()
            else -> run {
                sender.sendMessage("Unable to resolve sender user.".red())
                return
            }
        }

        val activePunishment = targetUser.getActivePunishment(PunishmentType.MUTE)
            ?: targetUser.getActivePunishment(PunishmentType.TEMPMUTE)

        if (activePunishment == null) {
            sender.sendMessage("${target.name} is not currently muted.".red())
            return
        }

        val success = targetUser.unmute(senderUser, activePunishment.id, reason)

        var format = ""
        var suffix: String
        var targetSuffix = ""

        if (sender is Player) {
            suffix = chatService?.getPlayerSuffix(sender)?.translate() ?: ""
            targetSuffix = if (Bukkit.getOnlinePlayers().contains(target.player)) chatService?.getPlayerSuffix(targetUser.player)?.translate() ?: "" else ""

            format = PunishmentService.config.logs.unMute
                .replace("%ranksuffix%", suffix)
                .replace("%targetSuffix", targetSuffix)
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                PlaceholderAPI.setPlaceholders(sender, format)
            }
        }
        if (success) {
            val msg = format.translate()
            val formatted = PunishmentService.buildRemovalMessage(
                msg,
                activePunishment,
                targetUser,
                senderUser,
                reason
            )
            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
            sender.sendMessage("Successfully unmuted ${targetSuffix}${target.name}.".blue())
        } else {
            sender.sendMessage("Failed to unmute ${targetSuffix}${target.name}.".red())
        }
    }
}

