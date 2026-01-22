package me.clearedSpore.sporeCore.menu.pms

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.menu.pms.item.MessageItem
import me.clearedSpore.sporeCore.user.User
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent


class PendingMsgsMenu(
    val user: User
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Pending Messages"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val msgs = user.pendingMessages

        msgs.forEachIndexed { index, msg ->
            addItem(MessageItem(msg, index + 1))
        }
    }


    override fun onClose(player: Player) {
        user.pendingMessages.clear()
        user.save()
    }


    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {
    }
}