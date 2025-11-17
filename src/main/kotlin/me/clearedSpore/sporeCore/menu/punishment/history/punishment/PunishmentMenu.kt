package me.clearedSpore.sporeCore.menu.punishment.history.punishment

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.punishment.history.HistoryMenu
import me.clearedSpore.sporeCore.menu.punishment.history.punishment.item.PunishmentItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent


class PunishmentMenu(
    val type: PunishmentType,
    val viewer: Player,
    val target: OfflinePlayer,
    val title: String,
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return title
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        startAutoRefresh()
        val user = UserManager.get(target)
        if (user == null) return

        val punishments = user.getPunishmentsByType(type)

        val sorted = punishments.sortedWith(compareByDescending<Punishment> { it.isActive() }
            .thenByDescending { it.punishDate })

        sorted.forEach { punishment ->
            addItem(PunishmentItem(punishment, viewer, target))
        }

        setGlobalMenuItem(5, 6, BackItem { player ->
            HistoryMenu(viewer, target).open(viewer)
        })
    }



    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}