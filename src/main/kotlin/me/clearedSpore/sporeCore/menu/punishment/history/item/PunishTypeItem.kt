package me.clearedSpore.sporeCore.menu.punishment.history.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.punishment.history.punishment.PunishmentMenu
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class PunishTypeItem(
    val type: PunishmentType,
    val name: String,
    val target: OfflinePlayer
)
    : Item() {


    override fun createItem(): ItemStack {
        val item = ItemStack(Material.BOOK)
        val meta = item.itemMeta

        meta.setDisplayName(name.blue())

        val user = UserManager.get(target)
        if(user == null){
            return NoUserItem.toItemStack()
        }

        val total = user.getPunishmentsByType(type).size

        val lore = mutableListOf<String>()
        lore.add("Total: &f$total".blue())

        meta.lore = lore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        PunishmentMenu(type, clicker, target, name).open(clicker)
    }
}