package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.annotation.RegisterListener
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

@RegisterListener
class LocationListener : Listener {


    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val player = event.player
        val user = UserManager.get(player) ?: return

        if (user.lastLocation != null) {
            user.lastLocation = player.location.clone()
        }

    }

    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        val from = event.from
        val user = UserManager.get(player) ?: return

        user.lastLocation = from

    }
}