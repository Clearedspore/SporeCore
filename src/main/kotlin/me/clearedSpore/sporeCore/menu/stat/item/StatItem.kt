package me.clearedSpore.sporeCore.menu.stat.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class StatItem(
    val player: OfflinePlayer,
    val stat: Statistic,
    val name: String,
    val material: Material
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta

        meta.setDisplayName(name.blue())
        meta.lore = listOf<String>(
            name.gray() + " ${player.getStatistic(stat).toString().blue()}",
        )

        item.itemMeta = meta

        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}