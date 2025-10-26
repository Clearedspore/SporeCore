package me.clearedSpore.sporeCore.features.warp.`object`

import org.bukkit.Bukkit
import org.bukkit.Location
import org.dizitart.no2.collection.Document

data class Warp(
    val name: String,
    val location: Location,
    val permission: String? = null
) {
    fun toDocument(): Document = Document.createDocument().apply {
        put("name", name)
        put("world", location.world?.name)
        put("x", location.x)
        put("y", location.y)
        put("z", location.z)
        put("yaw", location.yaw)
        put("pitch", location.pitch)
        permission?.let { put("permission", it) } // only store if not null
    }

    companion object {
        fun fromDocument(doc: Document): Warp? {
            val worldName = doc.get("world") as? String ?: return null
            val world = Bukkit.getWorld(worldName) ?: return null
            val x = (doc.get("x") as? Number)?.toDouble() ?: return null
            val y = (doc.get("y") as? Number)?.toDouble() ?: return null
            val z = (doc.get("z") as? Number)?.toDouble() ?: return null
            val yaw = (doc.get("yaw") as? Number)?.toFloat() ?: 0f
            val pitch = (doc.get("pitch") as? Number)?.toFloat() ?: 0f
            val name = doc.get("name") as? String ?: "Warp"
            val permission = doc.get("permission") as? String // may be null
            val loc = Location(world, x, y, z, yaw, pitch)
            return Warp(name, loc, permission)
        }
    }
}
