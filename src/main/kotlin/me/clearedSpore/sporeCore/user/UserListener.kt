package me.clearedSpore.sporeCore.user

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.util.Tasks
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserListener : Listener {

    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player
        val user = UserManager.get(player) ?: return

        // First join setup
        if (!user.hasJoinedBefore) {
            user.hasJoinedBefore = true
            user.firstJoin = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            val starter = SporeCore.instance.coreConfig.economy.starterBalance
            EconomyService.add(user, starter, "Starter balance")
        }

        // Track name changes
        if (user.playerName != player.name) {
            user.playerName = player.name
        }

        // Record join timestamp
        user.lastJoin = System.currentTimeMillis()
        UserManager.startAutoSave(user)

        Logger.infoDB("Loaded user data for ${player.name} (${player.uniqueId}) on login")
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = UserManager.getIfLoaded(player.uniqueId) ?: return

        if (user.pendingPayments.isNotEmpty()) {
            Tasks.runLater(Runnable {
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

        user.lastJoin = System.currentTimeMillis()
        UserManager.save(user)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = UserManager.getIfLoaded(player.uniqueId) ?: return

        val joinTime = user.lastJoin ?: System.currentTimeMillis()
        val quitTime = System.currentTimeMillis()
        val sessionDuration = quitTime - joinTime

        user.totalPlaytime += sessionDuration
        user.playtimeHistory.add(joinTime to quitTime)


        val twoWeeksAgo = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)
        user.playtimeHistory.removeIf { it.first < twoWeeksAgo }

        UserManager.save(user)
        UserManager.stopAutoSave(player.uniqueId)
        UserManager.remove(player.uniqueId)

        Logger.infoDB("Saved and removed user ${player.name} (${player.uniqueId}) on quit (session: ${sessionDuration / 1000}s)")
    }
}
