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

@CommandAlias("unban")
@CommandPermission(Perm.UNBAN)
class UnBanCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players @removalReasons")
    @Syntax("<player> <reason>")
    fun onUnban(sender: CommandSender, targetName: String, reason: String) {
        val target = Bukkit.getOfflinePlayer(targetName)
        val targetUser = UserManager.get(target)

        if (targetUser == null) {
            sender.userJoinFail()
            return
        }

        val senderUser: User? = when (sender) {
            is Player -> UserManager.get(sender) ?: run { sender.userFail(); return }
            is ConsoleCommandSender -> UserManager.getConsoleUser()
            else -> null
        }

        if (senderUser == null) {
            sender.sendMessage("Unable to resolve sender user.".red())
            return
        }

        val active = targetUser.getActivePunishment(PunishmentType.BAN)
            ?: targetUser.getActivePunishment(PunishmentType.TEMPBAN)

        if (active == null) {
            sender.sendMessage("${target.name} is not currently banned.".red())
            return
        }
        val success = targetUser.unban(senderUser, active.id, reason)
        var format = ""

        if (sender is Player) {
            var suffix = chatService?.getPlayerSuffix(sender)?.translate() ?: ""

            format = PunishmentService.config.logs.unBan
                .replace("%ranksuffix%", suffix)
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                PlaceholderAPI.setPlaceholders(sender, format)
            }
        }
        if (success) {
            val msg = format.translate()
            val formatted = PunishmentService.buildRemovalMessage(
                msg,
                active,
                targetUser,
                senderUser,
                reason
            )
            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
            sender.sendMessage("Successfully unbanned &f${target.name}.".blue())
        } else {
            sender.sendMessage("Failed to unban &f${target.name}.".red())
        }

    }
}
