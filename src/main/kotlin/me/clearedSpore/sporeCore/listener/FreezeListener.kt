package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.AutoListener
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

@AutoListener
class FreezeListener : Listener {

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player

        if (!player.hasMetadata("frozen")) return

        val from = event.from
        val to = event.to ?: return

        if (from.x != to.x || from.z != to.z || from.y != to.y) {
            event.isCancelled = true
            player.sendMessage("You can't move while being frozen!".red())
            return
        }
    }


    @EventHandler()
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player

        if (player.hasMetadata("frozen")) {
            event.isCancelled = true
            player.updateInventory()
            player.sendMessage("You can't place blocks while being frozen!".red())
            return
        }
    }

    @EventHandler()
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player

        if (player.hasMetadata("frozen")) {
            event.isCancelled = true
            player.updateInventory()
            player.sendMessage("You can't break blocks while being frozen!".red())
            return
        }
    }

    @EventHandler()
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (player.hasMetadata("frozen")) {
            event.isCancelled = true
            player.updateInventory()
            player.sendMessage("You can't interact while being frozen!".red())
            return
        }
    }

    @EventHandler()
    fun onDropItem(event: PlayerDropItemEvent) {
        val player = event.player

        if (player.hasMetadata("frozen")) {
            event.isCancelled = true
            player.updateInventory()
            player.sendMessage("You can't drop items while being frozen!".red())
            return
        }
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player

        if (!player.hasMetadata("frozen") || player.hasPermission(Perm.FREEZE_BYPASS))
            return

        val message = event.message.lowercase()
        val baseCommand = message.split(" ")[0]
        val allowed = SporeCore.instance.coreConfig.general.freezeCommands

        val isAllowed = allowed.any { baseCommand.startsWith(it.lowercase()) }

        if (!isAllowed) {
            event.isCancelled = true
            player.sendMessage("You can't run commands while being frozen!".red())
        }
    }


    @EventHandler()
    fun onBlockBreak(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) {
            return
        }

        val damager = event.damager


        if (damager.hasMetadata("frozen")) {
            event.isCancelled = true
            damager.sendMessage("You can't break blocks while being frozen!".red())
        }

        if (event.entity is Player && event.entity.hasMetadata("frozen")) {
            damager.sendMessage("That player is currently frozen and can't take damage!")
            return
        }
    }


}