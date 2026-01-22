package me.clearedSpore.sporeCore.features.kit

import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.features.kit.`object`.Kit
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

class KitService {

    private val db = DatabaseManager.getServerData()
    val kits: MutableMap<String, Kit> = mutableMapOf()

    init {
        reloadKits()
    }

    fun getAllKits(): List<Kit> = kits.values.toList()

    fun reloadKits() {
        kits.clear()
        Logger.info("Loading kits from database...")

        val allKits = db.kits
        if (allKits.isEmpty()) {
            Logger.info("No kits found in the database.")
            return
        }

        var loaded = 0
        var failed = 0

        for (kit in allKits) {
            try {
                if (kit.name.isBlank()) {
                    Logger.warn("Skipped invalid kit: missing name.")
                    failed++
                    continue
                }

                kits[kit.name.lowercase()] = kit
                loaded++
            } catch (ex: Exception) {
                Logger.warn("Failed to load kit '${kit.name}': ${ex.message}")
                failed++
            }
        }

        Logger.info("Loaded $loaded kits (${failed} failed).")
        kits.values.forEach { kit ->
            val perm = kit.permission ?: "None"
            Logger.info(" - ${kit.name} (Permission: $perm, Items: ${kit.inventory.size})")
        }
    }

    fun createKit(name: String, player: Player, permission: String? = null) {
        val key = name.lowercase()

        if (kits.containsKey(key)) {
            player.sendMessage("Kit '$name' already exists.".red())
            return
        }

        val inventory = player.inventory.contents.toList()
        val armor = player.inventory.armorContents.toList()
        val offHand = player.inventory.itemInOffHand

        val kit = Kit(
            name = name,
            id = UUID.randomUUID().toString(),
            inventory = inventory,
            armor = armor,
            offHand = offHand,
            permission = permission
        )
        kits[key] = kit
        db.kits.add(kit)

        DatabaseManager.saveServerData()
    }

    fun setDisplayItem(kitName: String, material: Material) {
        val key = kitName.lowercase()
        val kit = kits[key] ?: return Logger.warn("Kit '$kitName' not found.")

        val updated = kit.copy(displayItem = material)
        kits[key] = updated

        val index = db.kits.indexOfFirst { it.name.equals(kitName, ignoreCase = true) }
        if (index != -1) db.kits[index] = updated

        DatabaseManager.saveServerData()
        Logger.infoDB("Set display item for kit '$kitName' to $material")
    }

    fun setKitItems(kitName: String, player: Player) {
        val key = kitName.lowercase()
        val kit = kits[key] ?: return player.sendMessage("Kit '$kitName' not found.".red())

        val updated = kit.copy(
            inventory = player.inventory.contents.toList(),
            armor = player.inventory.armorContents.toList(),
            offHand = player.inventory.itemInOffHand
        )

        kits[key] = updated
        val index = db.kits.indexOfFirst { it.name.equals(kitName, ignoreCase = true) }
        if (index != -1) db.kits[index] = updated

        DatabaseManager.saveServerData()
        player.sendSuccessMessage("Updated items for kit ${kit.name.white()}.")
    }

    fun setCooldown(kitName: String, input: String) {
        val key = kitName.lowercase()
        val kit = kits[key] ?: return Logger.warn("Kit '$kitName' not found.")

        val duration = TimeUtil.parseDuration(input)
        if (duration <= 0) {
            Logger.warn("Invalid cooldown format: $input")
            return
        }

        val updated = kit.copy(cooldown = duration)
        kits[key] = updated

        val index = db.kits.indexOfFirst { it.name.equals(kitName, ignoreCase = true) }
        if (index != -1) db.kits[index] = updated

        DatabaseManager.saveServerData()
        Logger.infoDB("Set cooldown for kit '$kitName' to $input")
    }

    fun giveKit(player: Player, name: String) {
        val kit = kits[name.lowercase()]
        val user = UserManager.get(player.uniqueId)

        if (kit == null) {
            player.sendErrorMessage("Kit '$name' not found.")
            return
        }

        if (kit.permission != null && !player.hasPermission(kit.permission)) {
            player.sendErrorMessage("You don’t have permission to use this kit.")
            return
        }

        if (kit.cooldown != null && user != null && user.hasKitCooldown(kit.id)) {
            val remaining = user.getKitCooldownRemaining(kit.id)
            val formatted = TimeUtil.formatDuration(remaining, TimeUtil.TimeUnitStyle.LONG)
            player.sendErrorMessage("You must wait $formatted before using this kit again.")
            return
        }


        val inv = player.inventory
        var addedCount = 0
        var droppedCount = 0

        kit.inventory.forEachIndexed { index, item ->
            if (item == null) return@forEachIndexed
            if (kit.armor.contains(item) || item == kit.offHand) return@forEachIndexed

            val current = inv.getItem(index)
            if (current == null || current.type.isAir) {
                inv.setItem(index, item)
                addedCount++
            } else {
                val emptySlot = inv.firstEmpty()
                if (emptySlot != -1) {
                    inv.setItem(emptySlot, item)
                    addedCount++
                } else {
                    player.world.dropItemNaturally(player.location, item)
                    droppedCount++
                }
            }
        }

        kit.armor.forEachIndexed { index, item ->
            if (item == null) return@forEachIndexed
            val currentArmor = inv.armorContents[index]
            if (currentArmor == null || currentArmor.type.isAir) {
                val armor = inv.armorContents
                armor[index] = item
                inv.armorContents = armor
                addedCount++
            } else {
                val emptySlot = inv.firstEmpty()
                if (emptySlot != -1) {
                    inv.setItem(emptySlot, item)
                    addedCount++
                } else {
                    player.world.dropItemNaturally(player.location, item)
                    droppedCount++
                }
            }
        }

        kit.offHand?.let { offHandItem ->
            val currentOff = inv.itemInOffHand
            if (currentOff == null || currentOff.type.isAir) {
                inv.setItemInOffHand(offHandItem)
                addedCount++
            } else {
                val emptySlot = inv.firstEmpty()
                if (emptySlot != -1) {
                    inv.setItem(emptySlot, offHandItem)
                    addedCount++
                } else {
                    player.world.dropItemNaturally(player.location, offHandItem)
                    droppedCount++
                }
            }
        }

        player.updateInventory()

        player.sendSuccessMessage(
            "You have received kit ${kit.name.white()}"
        )

        if (kit.cooldown != null && user != null) {
            user.setKitCooldown(kit.id, kit.cooldown)
        }
    }

    fun deleteKit(name: String) {
        val key = name.lowercase()
        val removed = kits.remove(key)

        if (removed != null) {
            db.kits.removeIf { it.name.equals(name, ignoreCase = true) }
            DatabaseManager.saveServerData()
            Logger.info("Kit '$name' removed successfully.")
        } else {
            Logger.warn("Attempted to remove kit '$name', but it was not found.")
        }
    }

    fun setPermission(kitName: String, permission: String) {
        val key = kitName.lowercase()
        val kit = kits[key] ?: return Logger.warn("Kit '$kitName' not found.")

        val updated = kit.copy(permission = permission)
        kits[key] = updated

        val index = db.kits.indexOfFirst { it.name.equals(kitName, ignoreCase = true) }
        if (index != -1) db.kits[index] = updated

        DatabaseManager.saveServerData()
        Logger.infoDB("Permission for kit '$kitName' set to '$permission'")
    }

    fun deletePermission(kitName: String) {
        val key = kitName.lowercase()
        val kit = kits[key] ?: return Logger.warn("Kit '$kitName' not found.")

        val updated = kit.copy(permission = null)
        kits[key] = updated

        val index = db.kits.indexOfFirst { it.name.equals(kitName, ignoreCase = true) }
        if (index != -1) db.kits[index] = updated

        DatabaseManager.saveServerData()
        Logger.infoDB("Permission cleared for kit '$kitName'")
    }
}
