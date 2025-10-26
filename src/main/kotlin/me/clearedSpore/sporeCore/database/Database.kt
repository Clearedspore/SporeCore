package me.clearedSpore.sporeCore.database

import me.clearedSpore.sporeCore.database.util.DocReader
import me.clearedSpore.sporeCore.database.util.DocWriter
import me.clearedSpore.sporeCore.features.warp.`object`.Warp
import org.bukkit.Bukkit
import org.bukkit.Location
import org.dizitart.no2.collection.Document
import org.dizitart.no2.collection.NitriteCollection

data class Database(
    val id: String = "server",
    var spawn: Location? = null,
    var warps: MutableList<Warp> = mutableListOf()
) {

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

    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("spawn", locationToString(spawn))
        .putList("warps", warps.map { warpToDocument(it) })
        .build()

    fun save(collection: NitriteCollection) {
        val filter = org.dizitart.no2.filters.FluentFilter.where("id").eq(id)
        val doc = toDocument()
        val result = collection.update(filter, doc)
        if (result.affectedCount == 0) {
            collection.insert(doc)
        }
    }

    companion object {
        fun load(collection: NitriteCollection): Database {
            val docRaw = collection.find(org.dizitart.no2.filters.FluentFilter.where("id").eq("server")).firstOrNull()
            if (docRaw == null) {
                val db = Database()
                db.save(collection)
                return db
            }

            val doc = DocReader(docRaw)
            val warpDocs = doc.documents("warps")
            return Database(
                id = doc.string("id") ?: "server",
                spawn = doc.string("spawn")?.let { stringToLocation(it) },
                warps = warpDocs.mapNotNull { d ->
                    val name = d.get("name") as? String ?: return@mapNotNull null
                    val location = stringToLocation(d.get("location") as? String) ?: return@mapNotNull null
                    val permission = d.get("permission") as? String
                    Warp(name, location, permission)
                }.toMutableList()
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
