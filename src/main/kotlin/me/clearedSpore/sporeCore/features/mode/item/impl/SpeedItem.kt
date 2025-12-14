package me.clearedSpore.sporeCore.features.mode.item.impl
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.features.mode.item.`object`.ModeItem
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt


class SpeedItem : ModeItem("speed"), Listener {

    private val speedKey = "custom_speed"

    override fun getItemStack(): ItemStack {
        return ItemBuilder(Material.DIAMOND_SWORD)
            .addNBTTag("mode_item", id)
            .setName("Speed Item".blue())
            .addLoreLine("Right click to increase speed".gray())
            .addLoreLine("Left click to decrease speed".gray())
            .setGlow(true)
            .build()
    }


    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player
        val item = player.itemInHand
        if (!isItem(item)) return
        if (!canUse(player)) return

        if (!player.hasPermission(Perm.SPEED)) {
            player.sendMessage("You don't have permission to set your speed!".red())
            return
        }

        var speed = getCustomSpeed(player)

        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            speed = (speed + 1).coerceAtMost(10)
        }

        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            speed = (speed - 1).coerceAtLeast(-10)
        }

        val scaled = speed / 10f
        player.flySpeed = scaled
        player.walkSpeed = scaled

        triggerCooldown(player)
        player.sendMessage("Your speed has been set to &f$speed &7(&f$scaled&7)".blue())
    }

    private fun getCustomSpeed(player: Player): Int {
        val current = player.flySpeed
        return (current * 10).roundToInt()
    }
}