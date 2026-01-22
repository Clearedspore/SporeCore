package me.clearedSpore.sporeCore.task

import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeCore.util.ActionBar
import java.util.concurrent.TimeUnit

object ActionBarTicker {

    private var running = false


    fun start() {
        if (running) return
        running = true


        Task.runRepeated(
            Runnable {
                ActionBar.tick()
            },
            0,
            0,
            TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        if (!running) return
        running = false
    }
}