package me.clearedSpore.sporeCore.menu.stat.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class BalanceItem(
    val player: OfflinePlayer
) : Item() {
    override fun createItem(): ItemStack {
        val item = ItemStack(Material.GOLD_INGOT)
        val meta = item.itemMeta

        val user = UserManager.get(player)

        if (user == null) return NoUserItem.toItemStack()

        meta.setDisplayName("Balance".blue())
        meta.lore = listOf<String>(
            "Balance: ".gray() + EconomyService.format(user.balance).blue()
        )

        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}