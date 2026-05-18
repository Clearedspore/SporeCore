package me.clearedSpore.sporeCore.menu.invrollback.preview.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class InfoItem(
    private val itemName: String,
    private val material: Material,
    private val lore: List<String>
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(material)
            .setName(itemName)
            .setLore(lore)
            .build()
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}