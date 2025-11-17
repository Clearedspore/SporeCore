package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import io.papermc.paper.event.player.AsyncChatEvent

class PunishmentListener : Listener {

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val senderUser = UserManager.get(player) ?: run {
            player.userFail()
            event.viewers().clear()
            event.isCancelled = true
            return
        }

        if (senderUser.isMuted()) {
            val mute = senderUser.getActivePunishment(PunishmentType.MUTE)!!
            val msg = PunishmentService.getMessage(mute.type)
            val formatted = PunishmentService.buildMessage(msg, mute)
            player.sendMessage(formatted)

            event.viewers().clear()
            event.isCancelled = true


            if (PunishmentService.config.settings.notifyTry) {
                val tryMsgTemplate = when (mute.type) {
                    PunishmentType.MUTE -> PunishmentService.config.logs.tryMute
                    PunishmentType.TEMPMUTE -> PunishmentService.config.logs.tryTempMute
                    else -> return
                }

                val broadcastMsg = PunishmentService.buildTryMessage(tryMsgTemplate, mute, senderUser)
                Message.broadcastMessageWithPermission(broadcastMsg, Perm.PUNISH_LOG)
            }

            return
        }
    }
}