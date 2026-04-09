package me.clearedSpore.sporeCore.features.mode.item

import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.mode.item.impl.*
import me.clearedSpore.sporeCore.features.mode.item.`object`.ModeItem
import org.bukkit.Bukkit

object ModeItemManager {

    private val items = mutableMapOf<String, ModeItem>()

    fun registerItems() {
        register(FreezeItem())
        register(PunishSwordItem())
        register(SpeedItem())
        register(HistoryItem())
        register(InvseeItem())
        register(CompassItem())
        register(WorldEditItem())
    }

    fun register(item: ModeItem) {
        val plugin = SporeCore.instance
        items[item.id] = item
        Bukkit.getPluginManager().registerEvents(item, plugin)
    }

    fun getItem(id: String): ModeItem? = items[id]

    fun getAllItems(): Collection<ModeItem> = items.values
}
