package me.clearedSpore.sporeCore.features.currency.menu.main

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.currency.CurrencySystemService
import me.clearedSpore.sporeCore.features.currency.menu.main.item.CategoryItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class CurrencyMainMenu(private val player: Player) : Menu(SporeCore.instance) {

    private val service = CurrencySystemService
    val currencyName = service.currencyName

    private val menuSettings by lazy { service.getMenuSettingsFor("main") }

    override fun getMenuName(): String = service.config.currencySettings.shopName
    override fun getRows(): Int = menuSettings.rows
    override fun fillEmptySlots() = menuSettings.fillItems

    override fun setMenuItems() {
        service.getCategories().values.forEach { category ->
            val (row, column) = CurrencySystemService.parseSlot(category.slot)
            if (row > getRows()) {
                Logger.warn("[$currencyName] Category '${category.name}' cannot be in bottom row (row ${getRows()}). Skipping.")
                return@forEach
            }
            setMenuItem(column, row, CategoryItem(category, player))
        }


        service.config.shop.infoItems.values
            .filter { it.menu.isBlank() || it.menu.equals("main", ignoreCase = true) }
            .forEach { info ->
                val (row, column) = CurrencySystemService.parseSlot(info.slot)
                if (row > getRows()) {
                    Logger.warn("[$currencyName] Info item '${info.name}' cannot be in bottom row. Skipping.")
                    return@forEach
                }

                val material = Material.matchMaterial(info.material.uppercase()) ?: Material.BOOK
                val itemStack = ItemStack(material).apply {
                    val meta = itemMeta ?: return@apply
                    meta.setDisplayName(CurrencySystemService.parsePlaceholders(info.name.translate(), player))
                    meta.lore = info.description.map {
                        CurrencySystemService.parsePlaceholders(it, player).translate()
                    }
                    itemMeta = meta
                }

                setMenuItem(column, row, object : Item() {
                    override fun createItem(): ItemStack = itemStack
                    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
                })
            }
    }
}
