package me.clearedSpore.sporeCore.menu.util

import me.clearedSpore.sporeAPI.util.CC.red
import org.bukkit.Material
import org.bukkit.inventory.ItemStack


object NoUserItem {

    fun toItemStack(): ItemStack {
        val item = ItemStack(Material.BARRIER)
        val meta = item.itemMeta

        meta.setDisplayName("No data".red())
        meta.lore = listOf<String>(
            "Failed to load requested user data!".red()
        )

        item.itemMeta = meta
        return item
    }
}