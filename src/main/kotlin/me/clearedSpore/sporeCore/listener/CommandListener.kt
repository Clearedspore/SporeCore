package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class CommandListener : Listener {

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val sender = event.player
        val command = event.message

        if (SporeCore.instance.coreConfig.logs.commands) {
            LogsService.addLog(sender.uuidStr(), command, LogType.COMMAND)
        }
    }
}