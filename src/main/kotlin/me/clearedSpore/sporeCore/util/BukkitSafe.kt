package me.clearedSpore.sporeCore.util

import me.clearedSpore.sporeCore.extension.PlayerExtension.uuid
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object BukkitSafe {

    val nameToPlayerMap: ConcurrentHashMap<String, Player> = ConcurrentHashMap<String, Player>()
    val uuidToPlayerMap: ConcurrentHashMap<UUID, Player> = ConcurrentHashMap<UUID, Player>()

    fun getOnlinePlayers(): MutableCollection<Player> {
        return Collections.unmodifiableCollection<Player>(nameToPlayerMap.values)
    }

    fun getPlayer(name: String): Player? {
        return nameToPlayerMap[name]
    }

    fun getPlayer(uuid: UUID): Player? {
        return uuidToPlayerMap[uuid]
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        nameToPlayerMap.put(event.player.name, event.getPlayer())
        uuidToPlayerMap.put(event.player.uuid(), event.getPlayer())
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        nameToPlayerMap.remove(event.player.name)
        uuidToPlayerMap.remove(event.player.uuid())
    }
}