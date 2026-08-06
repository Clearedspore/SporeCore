package me.clearedSpore.sporeCore.util.doc

import me.clearedSpore.sporeAPI.serialization.codec.LocationCodec
import org.bukkit.Location
import org.dizitart.no2.collection.Document
import java.util.UUID

class DocWriter {
    private val doc: Document = Document.createDocument()

    fun put(key: String, value: Any?): DocWriter {
        if (value != null) doc.put(key, value)
        return this
    }

    fun putString(key: String, value: String?) = put(key, value)
    fun putBoolean(key: String, value: Boolean) = put(key, value)
    fun putInt(key: String, value: Int) = put(key, value)
    fun putLong(key: String, value: Long) = put(key, value)
    fun putDouble(key: String, value: Double) = put(key, value)

    fun putId(key: String, uuid: UUID?) = apply {
        if (uuid != null) doc.put(key, uuid.toString())
    }

    fun putLocation(key: String, location: Location?) = apply {
        location?.let {
            doc.put(key, LocationCodec().encode(it))
        }
    }

    fun putList(key: String, list: Collection<*>?) = apply {
        doc.put(key, list ?: emptyList<Any>())
    }

    fun putMap(key: String, map: Map<*, *>?) = apply {
        if (!map.isNullOrEmpty()) doc.put(key, map)
    }

    fun putDocuments(key: String, docs: Collection<Document>?) = apply {
        if (!docs.isNullOrEmpty()) doc.put(key, docs)
    }

    fun build(): Document = doc
}