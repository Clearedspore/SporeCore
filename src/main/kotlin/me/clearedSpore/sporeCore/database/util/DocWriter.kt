package me.clearedSpore.sporeCore.database.util

import org.bukkit.Location
import org.dizitart.no2.collection.Document

class DocWriter {
    private val doc: Document = Document.createDocument()

    fun put(key: String, value: Any?): DocWriter {
        if (value != null) doc.put(key, value)
        return this
    }

    fun putString(key: String, value: String?): DocWriter = put(key, value)
    fun putBoolean(key: String, value: Boolean): DocWriter = put(key, value)
    fun putInt(key: String, value: Int): DocWriter = put(key, value)
    fun putLong(key: String, value: Long): DocWriter = put(key, value)
    fun putDouble(key: String, value: Double): DocWriter = put(key, value)


    fun putLocation(key: String, location: Location?): DocWriter {
        location?.let {
            val str = "${it.world.name},${it.x},${it.y},${it.z},${it.yaw},${it.pitch}"
            doc.put(key, str)
        }
        return this
    }


    fun putList(key: String, list: Collection<*>?): DocWriter {
        if (!list.isNullOrEmpty()) doc.put(key, list)
        return this
    }

    fun putMap(key: String, map: Map<*, *>?): DocWriter {
        if (!map.isNullOrEmpty()) doc.put(key, map)
        return this
    }

    fun putDocuments(key: String, docs: Collection<Document>?): DocWriter {
        if (!docs.isNullOrEmpty()) doc.put(key, docs)
        return this
    }

    fun build(): Document = doc
}
