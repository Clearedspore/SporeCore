package me.clearedSpore.sporeCore.menu.baltop

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.menu.baltop.item.BalTopItem
import me.clearedSpore.sporeCore.menu.baltop.item.NoBalancesItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent


class BalTopMenu(private val viewer: Player) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String = "Baltop"
    override fun getRows(): Int = 6

    override fun createItems() {
        val maxPlayers = SporeCore.instance.coreConfig.economy.maxPlayers
        val topList = EconomyService.top(maxPlayers).join()

        if (topList.isEmpty()) {
            addItem(NoBalancesItem())
            return
        }

        addSearchItem(5, 6)

        topList.forEachIndexed { index, (player, balance) ->
            val displayName = player.name?.takeIf { it.isNotEmpty() } ?: "Unknown"
            addItem(BalTopItem(index + 1, displayName, balance))
        }
    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {
    }
}