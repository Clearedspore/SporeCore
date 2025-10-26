package me.clearedSpore.sporeCore.features.warp

import lombok.Getter
import lombok.Setter
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.features.warp.`object`.Warp
import org.bukkit.Location

@Getter
@Setter
class WarpService {

    private val db = DatabaseManager.getServerData()

    val warps: MutableMap<String, Warp> = mutableMapOf()

    init {
        reloadWarps()
    }

    fun getAllWarps(): List<Warp> {
        return warps.values.toList()
    }


    fun reloadWarps() {
        warps.clear()
        Logger.info("Reloading warps from database...")

        val allWarps = db.warps
        if (allWarps.isEmpty()) {
            Logger.info("No warps found in the database.")
            return
        }

        var loaded = 0
        var failed = 0

        for (warp in allWarps) {
            try {
                if (warp.name.isBlank()) {
                    Logger.warn("Skipped invalid warp: missing name.")
                    failed++
                    continue
                }

                if (warp.location.world == null) {
                    Logger.warn("Skipped warp '${warp.name}': world is null or unloaded.")
                    failed++
                    continue
                }

                warps[warp.name.lowercase()] = warp
                loaded++
            } catch (ex: Exception) {
                Logger.warn("Failed to load warp '${warp.name}': ${ex.message}")
                failed++
            }
        }

        Logger.info("Loaded $loaded warps (${failed} failed).")
        warps.values.forEach { warp ->
            val perm = warp.permission ?: "None"
            Logger.info(" - ${warp.name} (Permission: $perm)")
        }
    }

    fun createWarp(name: String, location: Location) {
        val key = name.lowercase()

        if (warps.containsKey(key)) {
            Logger.warn("Warp '$name' already exists. Skipping creation.")
            return
        }

        val warp = Warp(name, location, null)
        warps[key] = warp
        db.warps.add(warp)

        DatabaseManager.saveServerData()
        Logger.info("Created a new warp called '$name' at ${location.world?.name ?: "Unknown world"} [${location.blockX}, ${location.blockY}, ${location.blockZ}]")
    }

    fun deleteWarp(name: String) {
        val key = name.lowercase()
        val removed = warps.remove(key)

        if (removed != null) {
            db.warps.removeIf { it.name.equals(name, ignoreCase = true) }

            DatabaseManager.saveServerData()
            Logger.info("Warp '$name' removed successfully.")
        } else {
            Logger.warn("Attempted to remove warp '$name', but it was not found.")
        }
    }

    fun setPermission(warpName: String, permission: String) {
        val key = warpName.lowercase()
        val warp = warps[key]

        if (warp != null) {
            val updated = warp.copy(permission = permission)
            warps[key] = updated

            val index = db.warps.indexOfFirst { it.name.equals(warpName, ignoreCase = true) }
            if (index != -1) db.warps[index] = updated

            DatabaseManager.saveServerData()
            Logger.infoDB("Permission for warp '$warpName' set to '$permission'")
        } else {
            Logger.warn("Tried to set permission for warp '$warpName' but it was not found.")
        }
    }

    fun deletePermission(warpName: String) {
        val key = warpName.lowercase()
        val warp = warps[key]

        if (warp != null) {
            val updated = warp.copy()
            warps[key] = updated

            val index = db.warps.indexOfFirst { it.name.equals(warpName, ignoreCase = true) }
            if (index != -1) db.warps[index] = updated

            DatabaseManager.saveServerData()
            Logger.infoDB("Permission cleared for warp '$warpName'")
        } else {
            Logger.warn("Tried to clear permission for warp '$warpName' but it was not found.")
        }
    }
}
