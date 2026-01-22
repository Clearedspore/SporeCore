package me.clearedSpore.sporeCore.features.investigation.`object`.log

import me.clearedSpore.sporeCore.features.investigation.`object`.enum.IGLogType
import me.clearedSpore.sporeCore.features.investigation.`object`.note.Note
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.reports.`object`.ReportStatus
import me.clearedSpore.sporeCore.util.doc.DocReader
import me.clearedSpore.sporeCore.util.doc.DocWriter
import org.dizitart.no2.collection.Document
import org.dizitart.no2.repository.annotations.Id

data class IGLog(
    @Id val id: String,
    val user: String,
    val timestamp: Long,
    val action: String,
    val type: IGLogType,
){


    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("user", user)
        .put("timestamp", timestamp)
        .put("action", action)
        .put("type", type.name)
        .build()

    companion object {
        fun fromDocument(doc: Document): IGLog? {
            val reader = DocReader(doc)

            return IGLog(
                id = reader.string("id") ?: return null,
                user = reader.string("user") ?: return null,
                timestamp = reader.long("timestamp") ?: return null,
                action = reader.string("action") ?: return null,
                type = reader.enum<IGLogType>("type") ?: return null,
            )
        }

    }
}
