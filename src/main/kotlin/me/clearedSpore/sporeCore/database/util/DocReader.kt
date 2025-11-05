package me.clearedSpore.sporeCore.database.util

import org.bukkit.Bukkit
import org.bukkit.Location
import org.dizitart.no2.collection.Document

class DocReader(val doc: Document) {

    fun string(key: String): String? = doc.get(key)?.toString()

    fun boolean(key: String): Boolean =
        (doc.get(key) as? Boolean)
            ?: string(key)?.toBooleanStrictOrNull()
            ?: false

    fun int(key: String): Int =
        (doc.get(key) as? Number)?.toInt()
            ?: string(key)?.toIntOrNull()
            ?: 0

    fun long(key: String): Long =
        when (val value = doc.get(key)) {
            is Long -> value
            is Int -> value.toLong()
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }

    fun double(key: String): Double =
        (doc.get(key) as? Number)?.toDouble()
            ?: string(key)?.toDoubleOrNull()
            ?: 0.0


    fun location(key: String): Location? {
        val str = string(key) ?: return null
        val parts = str.split(",")
        if (parts.size != 6) return null
        val world = Bukkit.getWorld(parts[0]) ?: return null
        val x = parts[1].toDoubleOrNull() ?: return null
        val y = parts[2].toDoubleOrNull() ?: return null
        val z = parts[3].toDoubleOrNull() ?: return null
        val yaw = parts[4].toFloatOrNull() ?: 0f
        val pitch = parts[5].toFloatOrNull() ?: 0f
        return Location(world, x, y, z, yaw, pitch)
    }

    fun list(key: String): List<Any> =
        (doc.get(key) as? List<*>)?.filterNotNull() ?: emptyList()


    inline fun <reified T> map(key: String): Map<String, T> =
        (doc.get(key) as? Map<*, *>)?.mapNotNull {
            val k = it.key?.toString() ?: return@mapNotNull null
            val v: Any? = when (T::class) {
                Boolean::class -> (it.value as? Boolean ?: it.value.toString().toBooleanStrictOrNull())
                Double::class -> (it.value as? Number)?.toDouble() ?: it.value.toString().toDoubleOrNull()
                Number::class -> (it.value as? Number)
                    ?: it.value.toString().toDoubleOrNull()
                String::class -> it.value?.toString()
                else -> null
            }
            if (v == null) return@mapNotNull null
            k to (v as T)
        }?.toMap() ?: emptyMap()

    fun document(key: String): Document? =
        (doc.get(key) as? Document)

    fun documents(key: String): List<Document> =
        (doc.get(key) as? List<*>)?.filterIsInstance<Document>() ?: emptyList()
}
