package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.util.Tasks
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent

class InventoryListener : Listener {

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player
        val config = SporeCore.instance.coreConfig.inventories.storeReasons

        if (!config.death) return
        if (player.hasMetadata("NPC")) return

        var reason: String? = null

        val killer = player.killer
        if (killer != null) {
            reason = "Killed by ${killer.name}"
        } else {
            val cause = player.lastDamageCause
            reason = if (cause == null) {
                "Died"
            } else {
                when (cause.cause) {
                    EntityDamageEvent.DamageCause.FALL -> "Fell from a high place"
                    EntityDamageEvent.DamageCause.FIRE,
                    EntityDamageEvent.DamageCause.FIRE_TICK -> "Burned to death"
                    EntityDamageEvent.DamageCause.LAVA -> "Burned in lava"
                    EntityDamageEvent.DamageCause.DROWNING -> "Drowned"
                    EntityDamageEvent.DamageCause.SUFFOCATION -> "Suffocated in a wall"
                    EntityDamageEvent.DamageCause.VOID -> "Fell into the void"
                    EntityDamageEvent.DamageCause.LIGHTNING -> "Struck by lightning"
                    EntityDamageEvent.DamageCause.STARVATION -> "Starved to death"
                    EntityDamageEvent.DamageCause.POISON -> "Poisoned"
                    EntityDamageEvent.DamageCause.MAGIC -> "Killed by magic"
                    EntityDamageEvent.DamageCause.THORNS -> "Killed by thorns"
                    EntityDamageEvent.DamageCause.WITHER -> "Withered away"
                    EntityDamageEvent.DamageCause.ENTITY_ATTACK -> "Killed by an entity"
                    EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
                    EntityDamageEvent.DamageCause.BLOCK_EXPLOSION -> "Killed by an explosion"
                    EntityDamageEvent.DamageCause.PROJECTILE -> "Killed by a projectile"
                    EntityDamageEvent.DamageCause.CUSTOM -> "Died"
                    EntityDamageEvent.DamageCause.FLY_INTO_WALL -> "Flew into a wall"
                    EntityDamageEvent.DamageCause.HOT_FLOOR -> "Burned on magma"
                    EntityDamageEvent.DamageCause.CRAMMING -> "Died from cramming"
                    EntityDamageEvent.DamageCause.DRAGON_BREATH -> "Killed by dragon breath"
                    EntityDamageEvent.DamageCause.DRYOUT -> "Dried out"
                    EntityDamageEvent.DamageCause.FREEZE -> "Froze to death"
                    EntityDamageEvent.DamageCause.FALLING_BLOCK -> "Killed by a falling block"
                    EntityDamageEvent.DamageCause.MELTING -> "Melted"
                    else -> "Died"
                }
            }
        }

        Tasks.runAsync {
            InventoryManager.addPlayerInventory(player, reason)
        }
    }
}
