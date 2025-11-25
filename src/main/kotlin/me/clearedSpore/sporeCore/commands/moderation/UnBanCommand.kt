package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
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

        if (success) {
            val msg = PunishmentService.config.logs.unBan
            val formatted = PunishmentService.buildRemovalMessage(
                msg,
                active,
                targetUser,
                senderUser,
                reason
            )
            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
            sender.sendMessage("Successfully unbanned ${target.name}.".blue())
        } else {
            sender.sendMessage("Failed to unban ${target.name}.".red())
        }

    }
}
