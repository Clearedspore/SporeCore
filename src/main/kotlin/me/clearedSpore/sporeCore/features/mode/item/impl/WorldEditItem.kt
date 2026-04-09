package me.clearedSpore.sporeCore.features.mode.item.impl

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.mode.item.`object`.ModeItem
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack


class WorldEditItem : ModeItem("worldedit"), Listener {
    override fun getItemStack(): ItemStack {
            if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit") || Bukkit.getPluginManager()
                    .isPluginEnabled("FastAsyncWorldEdit")
            ) {
                val item = ItemBuilder(Material.WOODEN_AXE)
                    .addNBTTag("worldedit_item", id)
                    .setName("WorldEdit Wand".blue())
                    .addLoreLine("Left Click to set the first position".gray())
                    .addLoreLine("Right Click to set the second position".gray())
                    .setGlow(true)
                    .build()
                return item
            }
            Logger.info("WorldEdit is not installed on this server. The 'worldedit' item will not be given.")
            return ItemStack.empty()
        }
}