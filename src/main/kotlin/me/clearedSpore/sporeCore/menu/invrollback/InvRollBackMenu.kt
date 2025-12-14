package me.clearedSpore.sporeCore.menu.invrollback

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.menu.invrollback.item.InvItem
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.math.tan


class InvRollBackMenu(
    private val viewer: Player,
    private val player: OfflinePlayer
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Inv Rollback | ${player.name}"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        startAutoRefresh()
        InventoryManager.getInventoriesOf(player)
            .sortedByDescending { it.timestamp }
            .forEach { addItem(InvItem(viewer, it, player)) }
    }


    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}