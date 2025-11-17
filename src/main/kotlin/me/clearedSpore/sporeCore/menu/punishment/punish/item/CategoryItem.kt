package me.clearedSpore.sporeCore.menu.punishment.punish.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeCore.menu.punishment.punish.PunishCategoryMenu
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class CategoryItem(
    val player: Player,
    val target: OfflinePlayer,
    val category: String
) : Item() {
    override fun createItem(): ItemStack {
        val item = ItemStack(Material.BOOK)
        val meta = item.itemMeta!!
        meta.setDisplayName(category.blue().capitalizeFirstLetter())
        meta.lore = listOf("", "Click to view reasons in this category.".gray())
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        PunishCategoryMenu(clicker, target, category).open(clicker)
    }
}
