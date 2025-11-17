package me.clearedSpore.sporeCore.menu.punishment.punish

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.menu.punishment.punish.item.CategoryItem
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class PunishMenu(
    val player: Player,
    val target: OfflinePlayer
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Punish ${target.name}"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val categories = PunishmentService.config.reasons.categories

        if (categories.isEmpty()) {
            player.sendMessage("No punishment categories are configured.")
            return
        }

        for ((category, _) in categories) {
            addItem(CategoryItem(player, target, category))
        }
    }

    override fun onInventoryClickEvent(clicker: Player, clickType: ClickType, event: InventoryClickEvent) {
    }
}
