package me.clearedSpore.sporeCore.features.mode.listener

import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.mode.`object`.Mode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import java.util.*

class ModeListener(private val modeProvider: (Player) -> Mode?) : Listener {

    private val silentViewers = WeakHashMap<Player, Inventory>()

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val mode = modeProvider(damager) ?: return
        if (!mode.pvp) event.isCancelled = true
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val mode = modeProvider(event.player) ?: return
        if (!mode.blockBreak) event.isCancelled = true
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val mode = modeProvider(event.player) ?: return
        if (!mode.blockPlace) event.isCancelled = true
    }

    @EventHandler
    fun onPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val mode = modeProvider(player) ?: return
        if (!mode.itemPickup) event.isCancelled = true
    }

    @EventHandler
    fun onPickup(event: FoodLevelChangeEvent) {
        val player = event.entity as? Player ?: return
        val mode = modeProvider(player) ?: return
        event.isCancelled = true
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val mode = modeProvider(event.player) ?: return
        if (!mode.itemDrop) event.isCancelled = true
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        val mode = modeProvider(player) ?: return

        val blocked = mode.blockedCommands ?: return
        val command = event.message.lowercase().removePrefix("/")

        if (blocked.any { command.startsWith(it.lowercase()) }) {
            event.isCancelled = true
            player.sendErrorMessage("You cannot use this command in your current mode!")
        }
    }

    @EventHandler
    fun onInventory(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (silentViewers[player] == event.inventory) {
            event.isCancelled = true
            return
        }

        val mode = modeProvider(player) ?: return
        if (!mode.inventory) event.isCancelled = true
    }

    @EventHandler
    fun onSilentChest(event: PlayerInteractEvent) {
        val player = event.player
        val mode = modeProvider(player) ?: return
        if (!mode.silentChest) return

        val block = event.clickedBlock ?: return
        val type = block.type

        if (type == Material.CHEST || type == Material.TRAPPED_CHEST ||
            type == Material.ENDER_CHEST || type.name.contains("SHULKER_BOX")
        ) {

            event.isCancelled = true

            val blockState = block.state
            val inventory: Inventory = when (blockState) {
                is Container -> {
                    val name = (blockState.customName ?: type.name) + " (Silent view)".gray()
                    Bukkit.createInventory(player, blockState.inventory.size, name)
                }

                else -> return
            }

            if (blockState is Container) {
                inventory.contents = blockState.inventory.contents.clone()
            }

            silentViewers[player] = inventory
            player.openInventory(inventory)

            Bukkit.getScheduler().runTaskTimer(SporeCore.instance, Runnable {
                if (player.openInventory.topInventory != inventory) return@Runnable
                if (blockState is Container) {
                    inventory.contents = blockState.inventory.contents.clone()
                }
            }, 0L, 20L)
        }
    }
}
