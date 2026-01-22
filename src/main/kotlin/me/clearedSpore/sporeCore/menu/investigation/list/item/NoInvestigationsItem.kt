package me.clearedSpore.sporeCore.menu.investigation.list.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class NoInvestigationsItem : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.BARRIER)
            .setName("No active investigations".red())
            .setLore("There are currently no investigations!".gray())
            .build()

        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
    }
}