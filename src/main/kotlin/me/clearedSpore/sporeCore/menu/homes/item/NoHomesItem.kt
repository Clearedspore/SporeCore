package me.clearedSpore.sporeCore.menu.homes.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class NoHomesItem(
    val viewer: Player,
    val target: OfflinePlayer
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.BARRIER)
        val meta = item.itemMeta

        meta?.setDisplayName("No homes".red())

        val lore = mutableListOf<String>()
        if(target == viewer){
        lore.add("You don't have any homes!".white())
        } else {
            lore.add("${target.name} does not have any homes!".white())
        }

        meta?.lore = lore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}
