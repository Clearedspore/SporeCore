package me.clearedSpore.sporeCore.inventory

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.dizitart.no2.filters.FluentFilter
import java.util.*
import java.util.concurrent.TimeUnit

object InventoryManager {

    internal val inventoryCollection get() = DatabaseManager.getInventoryCollection()

    private val cachedInventories = mutableMapOf<String, InventoryData>()
    private var cleanupTask: BukkitTask? = null

    fun loadAllInventories() {
        inventoryCollection.find().forEach { doc ->
            InventoryData.fromDocument(doc)?.let { inv ->
                cachedInventories[inv.id] = inv
            }
        }
    }

    fun saveAllInventories() {
        val snapshot = cachedInventories.values.toList()
        snapshot.forEach { saveInventory(it) }
    }

    fun startCleanupTask() {
        cleanupTask?.cancel()
        cleanupTask = Task.runRepeatedAsync(
            Runnable { cleanupExpired() },
            delay = 0,
            interval = 1,
            unit = TimeUnit.HOURS
        )
    }

    fun hasPendingInventory(playerId: UUID): Boolean {
        val user = UserManager.get(playerId) ?: return false
        return user.pendingInventories.isNotEmpty()
    }


    fun stopCleanupTask() {
        cleanupTask?.cancel()
        cleanupTask = null
    }

    fun findInventory(id: String? = null, owner: String? = null): InventoryData? {
        if (id != null) return cachedInventories[id]
        if (owner != null) return cachedInventories.values.firstOrNull { it.owner == owner }
        return null
    }

    fun getInventory(id: String): InventoryData? = cachedInventories[id]

    fun removeInventory(id: String) {
        cachedInventories.remove(id)
        inventoryCollection.remove(FluentFilter.where("id").eq(id))
    }

    fun clearAll() {
        cachedInventories.clear()
        inventoryCollection.clear()
    }


    fun addPlayerInventory(player: Player, storeReason: String = ""): InventoryData {
        val id = UUID.randomUUID().toString()
        val data = InventoryData.fromPlayer(player, id, player.location, storeReason)
        putCached(data)
        return data
    }


    fun putCached(inv: InventoryData) {
        cachedInventories[inv.id] = inv
        saveInventory(inv)
    }

    private fun saveInventory(inventory: InventoryData) {
        Task.runAsync {
            val filter = FluentFilter.where("id").eq(inventory.id)
            val result = inventoryCollection.update(filter, inventory.toDocument())

            if (result.affectedCount == 0) {
                inventoryCollection.insert(inventory.toDocument())
            }
        }
    }


    fun restoreInventoryFor(playerId: UUID, inventory: InventoryData, mode: RestoreMode) {
        val onlinePlayer = SporeCore.instance.server.getPlayer(playerId)

        if (onlinePlayer != null) {
            when (mode) {
                RestoreMode.OVERRIDE -> {
                    restoreInventory(onlinePlayer, inventory)
                }

                RestoreMode.PENDING -> {
                    val user = UserManager.get(playerId)
                    if (user == null) {
                        Logger.error("Failed to restore inventory! Player not found!")
                        return
                    }

                    user.pendingInventories.add(inventory.id)
                    UserManager.save(user)
                }
            }
        } else {
            val user = UserManager.get(playerId)
            if (user == null) {
                Logger.error("Failed to restore inventory! Player not found!")
                return
            }
            user.pendingInventories.add(inventory.id)
        }
    }

    fun restoreInventory(player: Player, inventory: InventoryData) {
        val inv = player.inventory
        clearPlayerInventory(player)

        inv.contents = inventory.contents.toTypedArray()
        inv.armorContents = inventory.armor.toTypedArray()
        inv.setItemInOffHand(inventory.offhand)
        player.level = inventory.experience
    }

    fun getInventoriesOf(player: OfflinePlayer): List<InventoryData> {
        val uuid = player.uniqueId.toString()

        val cached = cachedInventories.values.filter { it.owner == uuid }

        val docs = inventoryCollection.find(FluentFilter.where("owner").eq(uuid))
        val loaded = docs.mapNotNull { InventoryData.fromDocument(it) }

        loaded.forEach { cachedInventories[it.id] = it }

        return (cached + loaded).distinctBy { it.id }
    }


    fun clearPlayerInventory(player: Player) {
        val inv = player.inventory
        inv.clear()
        inv.armorContents = arrayOfNulls(4)
        inv.setItemInOffHand(null)
    }


    fun cleanupExpired() {
        Logger.infoDB("Clearing inventories....")
        val config = SporeCore.instance.coreConfig.inventories
        val maxAge = TimeUtil.parseDuration(config.deletion)
        val now = System.currentTimeMillis()

        val toRemove = cachedInventories.values.filter { now - it.timestamp >= maxAge }

        if (toRemove.isEmpty()) return

        Logger.infoDB("Removing ${toRemove.size} inventories")

        toRemove.forEach {
            cachedInventories.remove(it.id)
            inventoryCollection.remove(FluentFilter.where("id").eq(it.id))
        }

        Logger.infoDB("Removed ${toRemove.size} inventories")
    }
}

enum class RestoreMode {
    OVERRIDE,
    PENDING
}
