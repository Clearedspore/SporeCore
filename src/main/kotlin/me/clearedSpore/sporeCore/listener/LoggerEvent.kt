package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.event.PlayerPreLogEvent
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.user.settings.Setting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class LoggerEvent : Listener {

    @EventHandler
    fun onPreLog(event: PlayerPreLogEvent) {
        val player = event.sender as? Player ?: return

        val user = UserManager.get(player)
        if (user == null) {
            player.userJoinFail()
            event.isCancelled = true
            return
        }

        if ((player.hasPermission(Perm.LOG) || player.hasPermission(Perm.ADMIN_LOG))
            && !user.isSettingEnabled(Setting.LOGS)
        ) {
            event.isCancelled = true
        }
    }
}
