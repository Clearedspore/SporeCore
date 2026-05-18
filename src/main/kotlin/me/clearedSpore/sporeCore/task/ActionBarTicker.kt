package me.clearedSpore.sporeCore.task

import me.clearedSpore.sporeAPI.task.Tasks
import me.clearedSpore.sporeCore.util.ActionBar
import java.util.concurrent.TimeUnit

object ActionBarTicker {

    private var running = false


    fun start() {
        if (running) return
        running = true


        Tasks.runRepeated(1, 1) {
                ActionBar.tick()
            }
    }

    fun stop() {
        if (!running) return
        running = false
    }
}