package me.clearedSpore.sporeCore.menu.rollback.item

import me.clearedSpore.sporeAPI.menu.Item
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class CancelRollbackItem : Item() {

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val meta = item.itemMeta
        meta.setDisplayName("§cCancel")
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        clicker.closeInventory()
    }
}
