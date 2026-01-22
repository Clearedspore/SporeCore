package me.clearedSpore.sporeCore

import me.clearedSpore.sporeAPI.util.ItemUtil
import me.clearedSpore.sporeCore.util.doc.DocReader
import me.clearedSpore.sporeCore.util.doc.DocWriter
import me.clearedSpore.sporeCore.features.currency.`object`.PackagePurchase
import me.clearedSpore.sporeCore.features.kit.`object`.Kit
import me.clearedSpore.sporeCore.features.warp.`object`.Warp
import me.clearedSpore.sporeCore.util.BukkitSafe
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.NitriteCollection
import org.dizitart.no2.filters.FluentFilter

data class Database(
    val id: String = "server",
    var totalJoins: Int = 0,
    var spawnString: String? = null,
    var warps: MutableList<Warp> = mutableListOf(),
    var kits: MutableList<Kit> = mutableListOf(),
    var packagePurchases: MutableList<PackagePurchase> = mutableListOf()
) {
    val spawn: Location?
        get() = stringToLocation(spawnString)

    private fun locationToString(loc: Location?): String? =
        loc?.let { "${it.world?.name},${it.x},${it.y},${it.z},${it.yaw},${it.pitch}" }

    private fun stringToLocation(str: String?): Location? {
        if (str.isNullOrEmpty()) return null
        val parts = str.split(",")
        if (parts.size != 6) return null
        val world = Bukkit.getWorld(parts[0]) ?: return null
        val x = parts[1].toDoubleOrNull() ?: return null
        val y = parts[2].toDoubleOrNull() ?: return null
        val z = parts[3].toDoubleOrNull() ?: return null
        val yaw = parts[4].toFloatOrNull() ?: return null
        val pitch = parts[5].toFloatOrNull() ?: return null
        return Location(world, x, y, z, yaw, pitch)
    }

    private fun warpToDocument(warp: Warp): Document = DocWriter()
        .put("name", warp.name)
        .put("permission", warp.permission)
        .put("location", locationToString(warp.location))
        .build()

    private fun packagePurchaseToDocument(packagePurchase: PackagePurchase): Document = DocWriter()
        .put("packageName", packagePurchase.packageName)
        .put("amount", packagePurchase.amount)
        .put("timestamp", packagePurchase.timestamp)
        .build()

    private fun kitToDocument(kit: Kit): Document = DocWriter()
        .put("name", kit.name)
        .put("id", kit.id)
        .put("inventory", ItemUtil.itemStackListToBase64(kit.inventory))
        .put("armor", ItemUtil.itemStackListToBase64(kit.armor))
        .put("offhand", ItemUtil.itemStackToBase64(kit.offHand))
        .put("permission", kit.permission)
        .put("cooldown", kit.cooldown)
        .put("displayItem", kit.displayItem)
        .build()

    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("spawn", spawnString)
        .putList("warps", warps.map { warpToDocument(it) })
        .putList("kits", kits.map { kitToDocument(it) })
        .putList("packagePurchases", packagePurchases.map { packagePurchaseToDocument(it) })
        .putInt("totalJoins", totalJoins)
        .build()

    fun save(collection: NitriteCollection) {
        val filter = FluentFilter.where("id").eq(id)
        val doc = toDocument()
        val result = collection.update(filter, doc)
        if (result.affectedCount == 0) {
            collection.insert(doc)
        }
    }

    companion object {
        fun load(collection: NitriteCollection): Database {
            val docRaw = collection.find(FluentFilter.where("id").eq("server")).firstOrNull()
            if (docRaw == null) {
                val db = Database()
                db.save(collection)
                return db
            }

            val doc = DocReader(docRaw)
            val warpDocs = doc.documents("warps")
            val kitDocs = doc.documents("kits")
            val packageDocs = doc.documents("packagePurchases")
            return Database(
                id = doc.string("id") ?: "server",
                spawnString = doc.string("spawn"),
                warps = warpDocs.mapNotNull { d ->
                    val name = d.get("name") as? String ?: return@mapNotNull null
                    val location = stringToLocation(d.get("location") as? String) ?: return@mapNotNull null
                    val permission = d.get("permission") as? String
                    Warp(name, location, permission)
                }.toMutableList(),
                kits = kitDocs.mapNotNull { d ->
                    val name = d.get("name") as? String ?: return@mapNotNull null
                    val id = d.get("id") as? String ?: return@mapNotNull null
                    val inventory = ItemUtil.itemStackListFromBase64(d.get("inventory") as? String)
                    val armor = ItemUtil.itemStackListFromBase64(d.get("armor") as? String)
                    val offhand = ItemUtil.itemStackFromBase64(d.get("offhand") as? String)
                    val permission = d.get("permission") as? String
                    val cooldown = when (val raw = d.get("cooldown")) {
                        is Long -> raw
                        is Int -> raw.toLong()
                        is Double -> raw.toLong()
                        else -> null
                    }
                    val displayItem = d.get("displayItem") as? Material
                    Kit(name, id, inventory, armor, offhand, permission, cooldown, displayItem)
                }.toMutableList(),
                packagePurchases = packageDocs.mapNotNull { d ->
                    val packageName = d.get("packageName") as? String ?: return@mapNotNull null
                    val amount = d.get("amount") as? Int ?: return@mapNotNull null
                    val timestamp = d.get("timestamp") as? Long ?: return@mapNotNull null
                    PackagePurchase(packageName, amount, timestamp)
                }.toMutableList(),
                totalJoins = doc.int("totalJoins")
            )
        }

        private fun stringToLocation(str: String?): Location? {
            if (str.isNullOrEmpty()) return null
            val parts = str.split(",")
            if (parts.size != 6) return null
            val world = Bukkit.getWorld(parts[0]) ?: return null
            val x = parts[1].toDoubleOrNull() ?: return null
            val y = parts[2].toDoubleOrNull() ?: return null
            val z = parts[3].toDoubleOrNull() ?: return null
            val yaw = parts[4].toFloatOrNull() ?: return null
            val pitch = parts[5].toFloatOrNull() ?: return null
            return Location(world, x, y, z, yaw, pitch)
        }
    }
}
