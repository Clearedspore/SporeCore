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
            is Player -> UserManager.get(sender)?.also {
                if (it == null) sender.userFail()
            }
            is ConsoleCommandSender -> UserManager.getConsoleUser()
            else -> null
        }

        if (senderUser == null) {
            sender.sendMessage("Unable to resolve sender user.".red())
            return
        }
        val msg = PunishmentService.config.logs.unBan

        if(targetUser.getActivePunishment(PunishmentType.BAN) == null){
            sender.sendMessage("${target.name} is not currently banned.".red())
            return
        }

        val updatedPunishment = targetUser.unban(senderUser, reason)
        if (updatedPunishment != null) {
            val formatted = PunishmentService.buildRemovalMessage(msg, updatedPunishment, senderUser, targetUser)
            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
            sender.sendMessage("Successfully unbanned ${target.name}.".blue())
        } else {
            sender.sendMessage("Failed to unban ${target.name}.".red())
        }

    }
}
