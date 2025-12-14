package me.clearedSpore.sporeCore.menu.invrollback.claim

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class ClaimMenu(
    private val player: Player
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String = "Claim inventory"

    override fun getRows(): Int = 6

    override fun createItems() {
        val user = UserManager.get(player)
        if (user == null) {
            player.closeInventory()
            return
        }

        user.pendingInventories
            .mapNotNull { InventoryManager.getInventory(it) }
            .sortedByDescending { it.timestamp }
            .forEach { addItem(ClaimItem(it.id)) }
    }


    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}
