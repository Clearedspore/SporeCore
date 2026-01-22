package me.clearedSpore.sporeCore.menu.util

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class NoDataItem(
    val name: String,
    val lore: String,
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.BARRIER)
            .setName(name.red())
            .setLore(lore.gray())
            .build()

        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
    }
}