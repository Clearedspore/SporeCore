package me.clearedSpore.sporeCore.menu.rollback

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.StaffPunishmentStats
import me.clearedSpore.sporeCore.menu.rollback.item.CancelRollbackItem
import me.clearedSpore.sporeCore.menu.rollback.item.ConfirmRollbackItem
import me.clearedSpore.sporeCore.menu.rollback.item.StaffRollbackPreviewItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class StaffRollbackPreviewMenu(
    val viewer: Player,
    val staff: OfflinePlayer,
    val timeArg: String,
    val stats: List<StaffPunishmentStats>
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String = "Rollback Preview - ${staff.name}"
    override fun getRows(): Int = 6

    override fun createItems() {
        startAutoRefresh()
        stats.forEach { stat ->
            addItem(StaffRollbackPreviewItem(stat, viewer, staff))
        }

        setGlobalMenuItem(4, 6, CancelRollbackItem())
        setGlobalMenuItem(6, 6, ConfirmRollbackItem(viewer, staff, timeArg, stats))
    }

    override fun onInventoryClickEvent(clicker: Player, clickType: org.bukkit.event.inventory.ClickType, event: org.bukkit.event.inventory.InventoryClickEvent) {}
}
