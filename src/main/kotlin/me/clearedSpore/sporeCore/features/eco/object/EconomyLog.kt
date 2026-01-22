package me.clearedSpore.sporeCore.features.eco.`object`

import me.clearedSpore.sporeCore.util.doc.DocWriter
import org.dizitart.no2.collection.Document

data class EconomyLog(
    val action: EcoAction,
    val amount: Double,
    val reason: String,
    val timestamp: Long
) {
    fun toDocument(): Document = DocWriter()
        .put("action", action.name)
        .put("amount", amount)
        .put("reason", reason)
        .put("timestamp", timestamp)
        .build()

    companion object {
        fun fromDocument(doc: Document): EconomyLog {
            val action = EcoAction.valueOf(doc.get("action") as? String ?: EcoAction.ADDED.name)
            val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
            val reason = doc.get("reason") as? String ?: ""
            val timestamp = when (val t = doc.get("timestamp")) {
                is Long -> t
                is Number -> t.toLong()
                is String -> t.toLongOrNull() ?: System.currentTimeMillis()
                else -> System.currentTimeMillis()
            }
            return EconomyLog(action, amount, reason, timestamp)
        }
    }

}
