package me.clearedSpore.sporeCore.features.stats

import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Bukkit

object PlaytimeTracker {
    private var taskId: Int? = null
    private const val INTERVAL_TICKS = 20L * 60L

    fun start() {
        val plugin = SporeCore.instance
        if (taskId != null) return

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, Runnable {
            val now = System.currentTimeMillis()

            Bukkit.getOnlinePlayers().forEach { player ->
                val user = UserManager.getIfLoaded(player.uniqueId) ?: return@forEach

                user.totalPlaytime += 60_000

                if (user.lastJoin == null) user.lastJoin = now

                user.playtimeHistory.add(now - 60_000 to now)

                val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000)
                user.playtimeHistory.removeIf { it.first < twoWeeksAgo }
            }
        }, INTERVAL_TICKS, INTERVAL_TICKS)
    }

    fun stop() {
        taskId?.let { Bukkit.getScheduler().cancelTask(it) }
        taskId = null
    }
}