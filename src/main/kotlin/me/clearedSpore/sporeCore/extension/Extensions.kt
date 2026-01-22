package me.clearedSpore.sporeCore.extension

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object Extensions {

    fun ItemStack?.isNullOrAir(): Boolean = this == null || type == Material.AIR


    fun Enum<*>.prettyName(): String {
        return name
            .lowercase()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }


}