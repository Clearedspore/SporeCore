package me.clearedSpore.sporeCore.currency.menu.main.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.currency.CurrencySystemService
import me.clearedSpore.sporeCore.currency.config.ShopCategoryConfig
import me.clearedSpore.sporeCore.currency.menu.category.CategoryMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class CategoryItem(
    private val category: ShopCategoryConfig,
    private val player: Player
) : Item() {

    override fun createItem(): ItemStack {
        val material = Material.matchMaterial(category.displayItem.material.uppercase()) ?: Material.PAPER
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item

        meta.setDisplayName(
            CurrencySystemService.parsePlaceholders(category.displayItem.name.toString(), player).translate()
        )
        meta.lore = category.displayItem.description.map {
            CurrencySystemService.parsePlaceholders(it, player).translate()
        }

        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val (row, _) = CurrencySystemService.parseSlot(category.slot)
        val maxRows = CurrencySystemService.config.shop.menuSettings.category.rows

        if (row > maxRows) {
            clicker.sendMessage("This category cannot be opened because it is in the bottom row!".red())
            return
        }

        if (clickType.isLeftClick || clickType.isRightClick) {
            CategoryMenu(category, player).open(clicker)
        }
    }
}
