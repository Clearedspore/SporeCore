package me.clearedSpore.sporeCore.features.currency.menu.category

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.currency.CurrencySystemService
import me.clearedSpore.sporeCore.features.currency.config.ShopCategoryConfig
import me.clearedSpore.sporeCore.features.currency.menu.main.CurrencyMainMenu
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class CategoryMenu(
    private val category: ShopCategoryConfig,
    private val player: Player
) : Menu(SporeCore.instance) {

    private val service = CurrencySystemService
    val currencyName = service.currencyName

    private val menuSettings by lazy { service.getMenuSettingsFor(category.name) }

    override fun getMenuName(): String = category.name
    override fun getRows(): Int = menuSettings.rows
    override fun fillEmptySlots(): Boolean = menuSettings.fillItems

    override fun setMenuItems() {
        category.items.values.forEach { itemConfig ->
            val (row, column) = CurrencySystemService.parseSlot(itemConfig.slot)
            if (row > getRows()) {
                Logger.warn("[$currencyName] Item '${itemConfig.name}' is in the bottom row. Skipping.")
                return@forEach
            }

            setMenuItem(column, row, object : Item() {
                override fun createItem(): ItemStack {
                    val mat = Material.matchMaterial(itemConfig.material.uppercase()) ?: run {
                        Logger.warn("[$currencyName] Invalid material '${itemConfig.material}' for item '${itemConfig.name}'. Skipping.")
                        return ItemStack(Material.AIR)
                    }

                    val stack = ItemStack(mat)
                    val meta = stack.itemMeta ?: return stack
                    meta.setDisplayName(CurrencySystemService.parsePlaceholders(itemConfig.name.translate(), player))
                    val lore = itemConfig.description.map {
                        CurrencySystemService.parsePlaceholders(it, player).translate()
                    }.toMutableList()

                    val user = UserManager.get(player) ?: return stack
                    val balance = CurrencySystemService.getBalance(user)
                    val ownsItem = CurrencySystemService.hasPermissionOrRank(player, itemConfig)

                    when {
                        ownsItem -> lore.add("&cYou already own this item.".translate())
                        balance < itemConfig.price -> lore.add("&cYou don't have enough ${service.currencyName}.".translate())
                        else -> lore.add("&aClick to buy!".translate())
                    }

                    meta.lore = lore
                    stack.itemMeta = meta
                    return stack
                }

                override fun onClickEvent(clicker: Player, clickType: ClickType) {
                    val user = UserManager.get(clicker) ?: run {
                        clicker.closeInventory()
                        clicker.userFail()
                        return
                    }

                    val balance = CurrencySystemService.getBalance(user)
                    val ownsItem = CurrencySystemService.hasPermissionOrRank(clicker, itemConfig)

                    if (ownsItem || balance < itemConfig.price) {
                        clicker.sendMessage(
                            when {
                                ownsItem -> "You already own ${itemConfig.name}".red()
                                else -> "You don't have enough ${service.currencyName}".red()
                            }
                        )
                        return
                    }

                    ConfirmMenu(clicker) {
                        CurrencySystemService.handlePurchase(clicker, itemConfig)
                    }.open(clicker)
                }
            })
        }


        service.config.shop.infoItems.values
            .filter { it.menu.isBlank() || it.menu.equals(category.name, ignoreCase = true) }
            .forEach { info ->
                val (row, column) = CurrencySystemService.parseSlot(info.slot)
                if (row > getRows()) {
                    Logger.warn("[$currencyName] Info item '${info.name}' cannot be in bottom row. Skipping.")
                    return@forEach
                }

                val mat = Material.matchMaterial(info.material.uppercase()) ?: Material.BOOK
                val stack = ItemStack(mat).apply {
                    val meta = itemMeta ?: return@apply
                    meta.setDisplayName(CurrencySystemService.parsePlaceholders(info.name, player).translate())
                    meta.lore = info.description.map {
                        CurrencySystemService.parsePlaceholders(it, player).translate()
                    }
                    itemMeta = meta
                }

                setMenuItem(column, row, object : Item() {
                    override fun createItem(): ItemStack = stack
                    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
                })
            }


        val backSlot = 5
        setMenuItem(backSlot, getRows(), object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.BARRIER)
                val meta = item.itemMeta ?: return item
                meta.setDisplayName("Back".red())
                item.itemMeta = meta
                return item
            }

            override fun onClickEvent(clicker: Player, clickType: ClickType) {
                CurrencyMainMenu(clicker).open(clicker)
            }
        })
    }
}
