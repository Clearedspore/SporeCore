package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeCore.annotations.AutoListener
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

@AutoListener
class LocationListener : Listener {


    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val player = event.player
        val user = UserManager.get(player)
        if (user == null) return

        if (user.lastLocation != null) {
            user.lastLocation = null
        }

    }

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        val from = event.from
        val user = UserManager.get(player)
        if (user == null) return

        user.lastLocation = from

    }
}