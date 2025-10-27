package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.user.settings.Setting
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Tasks
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatEvent : Listener {

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val sender = event.player

        val senderUser = UserManager.get(sender)
        if (senderUser == null) {
            sender.userFail()
            event.isCancelled = true
            return
        }

        if (!senderUser.isSettingEnabled(Setting.CHAT_ENABLED)) {
            event.isCancelled = true
            sender.sendErrorMessage("You can't send messages while having chat disabled!")
            return
        }

        val toRemove = event.recipients.filter { recipient ->
            val recipientUser = UserManager.get(recipient)

            if (recipientUser == null) return@filter true

            !sender.hasPermission(Perm.CHAT_BYPASS) && !recipientUser.isSettingEnabled(Setting.CHAT_ENABLED)
        }

        Tasks.run {
            event.recipients.removeAll(toRemove)
        }

        val config = SporeCore.instance.coreConfig
        if(sender.hasPermission(Perm.COLORED_CHAT) && config.features.coloredChat) {
            event.message = event.message.translate()
        }
    }
}
