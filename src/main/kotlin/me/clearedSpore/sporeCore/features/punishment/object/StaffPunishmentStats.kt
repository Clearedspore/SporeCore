package me.clearedSpore.sporeCore.features.punishment.`object`

import org.dizitart.no2.collection.Document
import me.clearedSpore.sporeCore.database.util.DocWriter
import java.util.*

data class StaffPunishmentStats(
    val targetUuid: UUID,
    val type: PunishmentType,
    val date: Date = Date(),
    val punishmentId: String,
    val reason: String
) {
    fun toDocument(): Document = DocWriter()
        .put("targetUuid", targetUuid.toString())
        .put("type", type.name)
        .put("date", date.time)
        .put("punishmentId", punishmentId)
        .put("reason", reason)
        .build()

    companion object {
        fun fromDocument(doc: Document): StaffPunishmentStats? {
            val targetUuid = runCatching { UUID.fromString(doc.get("targetUuid", String::class.java)) }.getOrNull()
                ?: return null
            val typeName = doc.get("type", String::class.java) ?: return null
            val type = runCatching { PunishmentType.valueOf(typeName) }.getOrNull() ?: return null
            val date = (doc.get("date") as? Number)?.let { Date(it.toLong()) } ?: Date()
            val punishmentId = doc.get("punishmentId", String::class.java) ?: return null
            val reason = doc.get("reason", String::class.java) ?: "No reason"

            return StaffPunishmentStats(targetUuid, type, date, punishmentId, reason)
        }
    }
}
