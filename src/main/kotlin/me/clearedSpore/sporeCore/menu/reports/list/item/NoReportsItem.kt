package me.clearedSpore.sporeCore.menu.reports.list.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class NoReportsItem : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.BARRIER)
            .setName("No Reports".red())
            .setLore("There are currently no reports!".gray())
            .build()

        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
    }
}