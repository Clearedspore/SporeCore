package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.StringUtil.firstPart
import me.clearedSpore.sporeAPI.util.StringUtil.hasFlag
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.AutoListener
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent

@AutoListener
class DeathListener : Listener {

    @EventHandler
    fun onDeath(event: PlayerRespawnEvent) {
        val player = event.player
        val config = SporeCore.instance.coreConfig
        val kitService = SporeCore.instance.kitService

        if (config.kits.deathKit.isNotEmpty()) {
            val kitName = config.kits.deathKit.firstPart()
            val shouldClear = config.kits.deathKit.hasFlag("clear")
            val kits = kitService.getAllKits()

            val kit = kits.find { it.name.equals(kitName, ignoreCase = true) }

            if (kit == null) {
                Logger.error("Failed to give death kit to ${player.name}")
                return
            }

            if (shouldClear) {
                player.inventory.clear()
            }

            kitService.giveKit(player, kitName)
        }
    }

}