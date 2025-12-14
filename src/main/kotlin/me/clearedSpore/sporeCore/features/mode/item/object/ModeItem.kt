package me.clearedSpore.sporeCore.features.mode.item.`object`

import me.clearedSpore.sporeAPI.util.ItemUtil
import me.clearedSpore.sporeCore.SporeCore
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class ModeItem(val id: String) : Listener {

    private val spamCooldown = mutableMapOf<UUID, Long>()
    private val COOLDOWN_TICKS: Long = 20L

    abstract fun getItemStack(): ItemStack

    fun isItem(stack: ItemStack?): Boolean {
        if (stack == null) return false
        return ItemUtil.hasNBTTag(SporeCore.instance, stack, "mode_item") &&
                ItemUtil.getNBTTag(SporeCore.instance, stack, "mode_item") == id
    }

    fun canUse(player: Player): Boolean {
        val lastUsed = spamCooldown[player.uniqueId] ?: 0
        val now = System.currentTimeMillis()
        val cooldownMillis = (COOLDOWN_TICKS * 50)
        return now - lastUsed >= cooldownMillis
    }

    fun triggerCooldown(player: Player) {
        spamCooldown[player.uniqueId] = System.currentTimeMillis()
    }

}
