package me.clearedSpore.sporeCore.user

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.util.Tasks
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.TimeUnit

class UserListener : Listener {

    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player

        val user = UserManager.get(player) ?: return

        if (!user.hasJoinedBefore) {
            user.hasJoinedBefore = true
            user.firstJoin = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val starter = SporeCore.instance.coreConfig.economy.starterBalance
            EconomyService.add(user, starter, "Starter balance")
        }

        if (user.playerName != player.name) {
            user.playerName = player.name
        }

        UserManager.startAutoSave(user)

        Logger.infoDB("Loaded user data for ${player.name} (${player.uniqueId}) on login")
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        val user = UserManager.getIfLoaded(player.uniqueId) ?: return

        if (user.pendingPayments.isNotEmpty()) {
            Tasks.runLater(Runnable{
            player.sendMessage("")
            user.pendingPayments.forEach { (senderName, total) ->
                val formattedAmount = EconomyService.format(total)
                player.sendMessage("You received ${formattedAmount.green()} from ${senderName.white()}".blue())
            }
            player.sendMessage("")
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
            user.pendingPayments.clear()
            UserManager.save(user)
            }, 1)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = UserManager.getIfLoaded(player.uniqueId) ?: return

        UserManager.save(user)
        UserManager.stopAutoSave(player.uniqueId)
        UserManager.remove(player.uniqueId)

        Logger.infoDB("Saved and removed user ${player.name} (${player.uniqueId}) on quit")
    }
}
