package me.clearedSpore.sporeCore.util

import me.clearedSpore.sporeCore.SporeCore
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

object Tasks {

    fun run(runnable: Runnable): BukkitTask =
        Bukkit.getScheduler().runTask(SporeCore.instance, runnable)

    fun runSync(runnable: Runnable): BukkitTask =
        Bukkit.getScheduler().runTask(SporeCore.instance, runnable)

    fun runAsync(runnable: Runnable): BukkitTask =
        Bukkit.getScheduler().runTaskAsynchronously(SporeCore.instance, runnable)

    fun runLater(runnable: Runnable, delay: Long, unit: TimeUnit = TimeUnit.SECONDS): BukkitTask =
        Bukkit.getScheduler().runTaskLater(
            SporeCore.instance,
            runnable,
            unit.toSeconds(delay) * 20L
        )

    fun runLaterAsync(runnable: Runnable, delay: Long, unit: TimeUnit = TimeUnit.SECONDS): BukkitTask =
        Bukkit.getScheduler().runTaskLaterAsynchronously(
            SporeCore.instance,
            runnable,
            unit.toSeconds(delay) * 20L
        )

    fun runRepeated(runnable: Runnable, delay: Long, interval: Long, unit: TimeUnit = TimeUnit.SECONDS): BukkitTask =
        Bukkit.getScheduler().runTaskTimer(
            SporeCore.instance,
            runnable,
            unit.toSeconds(delay) * 20L,
            unit.toSeconds(interval) * 20L
        )

    fun runRepeatedAsync(runnable: Runnable, delay: Long, interval: Long, unit: TimeUnit = TimeUnit.SECONDS): BukkitTask =
        Bukkit.getScheduler().runTaskTimerAsynchronously(
            SporeCore.instance,
            runnable,
            unit.toSeconds(delay) * 20L,
            unit.toSeconds(interval) * 20L
        )
}
