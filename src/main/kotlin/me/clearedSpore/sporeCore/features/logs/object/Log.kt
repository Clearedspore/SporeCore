package me.clearedSpore.sporeCore.features.logs.`object`

import me.clearedSpore.sporeCore.util.doc.DocWriter
import org.dizitart.no2.collection.Document


data class Log(
    var id: String,
    var sender: String,
    var type: LogType,
    var action: String,
    var timestamp: Long
) {
    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("sender", sender)
        .put("type", type.name)
        .put("action", action)
        .put("timestamp", timestamp)
        .build()

    companion object {
        fun fromDocument(doc: Document): Log {
            val id = doc.get("id") as? String ?: ""
            val sender = doc.get("sender") as? String ?: ""
            val type = LogType.valueOf(doc.get("type") as? String ?: LogType.CHAT.name)
            val action = doc.get("action") as? String ?: ""
            val timestamp = when (val t = doc.get("timestamp")) {
                is Long -> t
                is Number -> t.toLong()
                is String -> t.toLongOrNull() ?: System.currentTimeMillis()
                else -> System.currentTimeMillis()
            }

            return Log(id, sender, type, action, timestamp)
        }
    }
}
