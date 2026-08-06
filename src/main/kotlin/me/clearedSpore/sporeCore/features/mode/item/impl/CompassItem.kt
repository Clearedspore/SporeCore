package me.clearedSpore.sporeCore.features.mode.item.impl

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeCore.features.mode.item.`object`.ModeItem
import me.clearedSpore.sporeAPI.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


class CompassItem : ModeItem("teleporter"), Listener {

    override fun getItemStack(): ItemStack {
        val item = ItemBuilder(Material.COMPASS)
            .addNBTTag("mode_item", id)
            .setName("Teleporter".blue())
            .addLoreLine("Left Click to Jump to wherever you look".gray())
            .addLoreLine("Right Click to go through wherever you look".gray())
            .setGlow(true)
            .build()
        return item
    }
}