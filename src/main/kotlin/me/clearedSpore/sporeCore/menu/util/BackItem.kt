package me.clearedSpore.sporeCore.menu.util

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.red
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class BackItem(private val onClick: (Player) -> Unit) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.BARRIER)
        val meta = item.itemMeta
        meta?.setDisplayName("&lBack".red())
        meta?.lore = listOf("Click to go back".red())
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        onClick(clicker)
    }
}
