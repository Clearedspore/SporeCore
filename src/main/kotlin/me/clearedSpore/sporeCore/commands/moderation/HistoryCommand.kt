package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.menu.punishment.history.HistoryMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.Date

@CommandAlias("history|viewpunishments|checkpunishments")
@CommandPermission(Perm.HISTORY)
class HistoryCommand : BaseCommand() {

    @Default()
    @CommandCompletion("@players")
    @Syntax("[player]")
    fun onCheck(player: Player, @Optional targetName: String?){

        if(targetName != null){
            if(!player.hasPermission(Perm.HISTORY_OTHERS)){
                player.sendErrorMessage("You don't have permission to view other players their history!")
                return
            }
            val target = Bukkit.getOfflinePlayer(targetName)

            if(!target.hasPlayedBefore()){
                player.userJoinFail()
                return
            }

            val targetUser = UserManager.get(target)
            if (targetUser != null) {
                targetUser.punishments.forEachIndexed { index, punishment ->
                    if (punishment.expireDate != null && punishment.removalDate == null && Date().after(punishment.expireDate)) {
                        val updated = punishment.copy(
                            removalReason = "Expired",
                            removalUserUuid = Punishment.SYSTEM_UUID,
                            removalDate = Date()
                        )
                        targetUser.punishments[index] = updated
                    }
                }
                UserManager.save(targetUser)
            }

            HistoryMenu(player, target).open(player)
        } else {

            val user = UserManager.get(player)
            if (user != null) {
                user.punishments.forEachIndexed { index, punishment ->
                    if (punishment.expireDate != null && punishment.removalDate == null && Date().after(punishment.expireDate)) {
                        val updated = punishment.copy(
                            removalReason = "Expired",
                            removalUserUuid = Punishment.SYSTEM_UUID,
                            removalDate = Date()
                        )
                        user.punishments[index] = updated
                    }
                }
                UserManager.save(user)
            }

            HistoryMenu(player, player).open(player)
        }

    }
}