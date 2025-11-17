package me.clearedSpore.sporeCore.menu.punishment.punish

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.menu.punishment.punish.PunishMenu
import me.clearedSpore.sporeCore.menu.punishment.punish.item.PunishItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class PunishCategoryMenu(
    val player: Player,
    val target: OfflinePlayer,
    val category: String
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Reasons | ${category.replaceFirstChar { it.uppercase() }}"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val reasons = PunishmentService.config.reasons.categories[category] ?: return

        for ((reasonKey, reasonDef) in reasons) {
            addItem(PunishItem(player, target, category, reasonKey, reasonDef))
        }


        setGlobalMenuItem(5, 6, BackItem { viewer ->
            PunishMenu(viewer, target).open(viewer)
        })
    }

    override fun onInventoryClickEvent(clicker: Player, clickType: ClickType, event: InventoryClickEvent) {}
}
