package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.menu.pms.PendingMsgsMenu
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.entity.Player

@CommandAlias("pendingmsg|pms|checkmessages")
class PendingMSGCommand : BaseCommand() {

    @Default()
    fun onPendingMsgs(player: Player){
        val user = UserManager.get(player)

        if(user == null){
            player.userFail()
            return
        }

        if(user.pendingMessages.isEmpty()){
            player.sendErrorMessage("You don't have any pending messages!")
            return
        }

        PendingMsgsMenu(user).open(player)

    }

}