package me.clearedSpore.sporeCore.features.reports.`object`

import me.clearedSpore.sporeCore.util.doc.DocReader
import me.clearedSpore.sporeCore.util.doc.DocWriter
import org.dizitart.no2.collection.Document
import org.dizitart.no2.repository.annotations.Id

data class Report(
    @Id var id: String,
    var targetUuid: String,
    var targetName: String,
    var targetSuffix: String,
    var reporterUuid: String,
    var reporterName: String,
    var reporterSuffix: String,
    var reason: String,
    var evidence: String? = null,
    var timestamp: Long,
    var type: ReportType,
    var status: ReportStatus,
    var action: ReportAction,
    var staffName: String? = null,
    var staffUuid: String? = null,
    var staffSuffix: String? = null,
    var silent: Boolean = false
) {

    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("targetUuid", targetUuid)
        .put("targetName", targetName)
        .put("targetSuffix", targetSuffix)
        .put("reporterUuid", reporterUuid)
        .put("reporterName", reporterName)
        .put("reporterSuffix", reporterSuffix)
        .put("reason", reason)
        .put("evidence", evidence)
        .put("timestamp", timestamp)
        .put("type", type.name)
        .put("status", status.name)
        .put("action", action.name)
        .put("staffName", staffName)
        .put("staffUuid", staffUuid)
        .put("staffSuffix", staffSuffix)
        .putBoolean("silent", silent)
        .build()

    companion object {
        fun fromDocument(doc: Document): Report? {
            val reader = DocReader(doc)

            return Report(
                id = reader.string("id") ?: return null,
                targetUuid = reader.string("targetUuid") ?: return null,
                targetName = reader.string("targetName") ?: "Unknown",
                targetSuffix = reader.string("targetSuffix") ?: "Unknown",
                reporterUuid = reader.string("reporterUuid") ?: return null,
                reporterName = reader.string("reporterName") ?: "Unknown",
                reporterSuffix = reader.string("reporterSuffix") ?: "Unknown",
                reason = reader.string("reason") ?: return null,
                evidence = reader.string("evidence"),
                timestamp = reader.long("timestamp"),
                type = reader.enum<ReportType>("type") ?: return null,
                status = reader.enum<ReportStatus>("status") ?: return null,
                action = reader.enum<ReportAction>("action") ?: return null,
                staffName = reader.string("staffName"),
                staffUuid = reader.string("staffUuid"),
                staffSuffix = reader.string("staffSuffix"),
                silent = reader.boolean("silent")
            )
        }

    }
}
