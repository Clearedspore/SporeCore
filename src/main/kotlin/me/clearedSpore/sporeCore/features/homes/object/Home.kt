package me.clearedSpore.sporeCore.features.homes.`object`

import org.bukkit.Bukkit
import org.bukkit.Location
import org.dizitart.no2.collection.Document

data class Home(
    val name: String,
    val location: Location
) {
    fun toDocument(): Document = Document.createDocument().apply {
        put("name", name)
        put("world", location.world?.name)
        put("x", location.x)
        put("y", location.y)
        put("z", location.z)
        put("yaw", location.yaw)
        put("pitch", location.pitch)
    }

    companion object {
        fun fromDocument(doc: Document): Home? {
            val world = Bukkit.getWorld(doc.get("world") as? String ?: return null) ?: return null
            val x = (doc.get("x") as? Number)?.toDouble() ?: return null
            val y = (doc.get("y") as? Number)?.toDouble() ?: return null
            val z = (doc.get("z") as? Number)?.toDouble() ?: return null
            val yaw = (doc.get("yaw") as? Number)?.toFloat() ?: 0f
            val pitch = (doc.get("pitch") as? Number)?.toFloat() ?: 0f
            val loc = Location(world, x, y, z, yaw, pitch)
            return Home(doc.get("name") as? String ?: "Home", loc)
        }
    }
}
