package me.clearedSpore.sporeCore.features.investigation.`object`

import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.yellow
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationPriority
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationStatus
import me.clearedSpore.sporeCore.features.investigation.`object`.log.IGLog
import me.clearedSpore.sporeCore.features.investigation.`object`.note.Note
import me.clearedSpore.sporeCore.features.investigation.`object`.suspect.Suspect
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.util.doc.DocReader
import me.clearedSpore.sporeCore.util.doc.DocWriter
import org.bukkit.Bukkit
import org.dizitart.no2.collection.Document
import org.dizitart.no2.repository.annotations.Id
import java.util.UUID

data class Investigation(
    @Id var id: String,
    var name: String,
    var creator: String,
    var description: String,
    var timestamp: Long,
    var notes: MutableList<Note>,
    var linkedReports: MutableList<Report>,
    var linkedPunishments: MutableList<Punishment>,
    var suspects: MutableList<Suspect>,
    var staff: MutableList<String>,
    var admin: MutableList<String>,
    var logs: MutableList<IGLog>,
    var status: InvestigationStatus,
    var priority: InvestigationPriority,
) {

    fun getCreatorName() : String? {
        val player = Bukkit.getOfflinePlayer(creator)
        return player.name
    }

    fun getName(staffID: String): String {
        val player = Bukkit.getOfflinePlayer(UUID.fromString(staffID))
        return player.name.toString()
    }


    fun getPriorityText() : String {
        return when (priority) {
            InvestigationPriority.HIGH -> "&lHigh".red()
            InvestigationPriority.NORMAL -> "Normal".gold()
            InvestigationPriority.LOW -> "Low".yellow()
        }
    }

    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("name", name)
        .put("creator", creator)
        .put("description", description)
        .put("timestamp", timestamp)
        .putList("notes", notes.map { it.toDocument() })
        .putList("linkedReports", linkedReports.map { it.toDocument() })
        .putList("linkedPunishments", linkedPunishments.map { it.toDocument() })
        .putList("suspects", suspects.map { it.toDocument() })
        .putList("logs", logs.map { it.toDocument() })
        .putList("staff", staff)
        .putList("admin", admin)
        .put("status", status.name)
        .put("priority", priority.name)
        .build()


    companion object {
        fun fromDocument(doc: Document): Investigation? {
            val reader = DocReader(doc)

            return Investigation(
                id = reader.string("id") ?: return null,
                name = reader.string("name") ?: return null,
                creator = reader.string("creator") ?: return null,
                description = reader.string("description") ?: return null,
                timestamp = reader.long("timestamp"),
                notes = reader.list("notes")
                    .filterIsInstance<Document>()
                    .mapNotNull { Note.fromDocument(it) }
                    .toMutableList(),
                linkedReports = reader.list("linkedReports")
                    .filterIsInstance<Document>()
                    .mapNotNull { Report.fromDocument(it) }
                    .toMutableList(),
                linkedPunishments = reader.list("linkedPunishments")
                    .filterIsInstance<Document>()
                    .mapNotNull { Punishment.fromDocument(it) }
                    .toMutableList(),
                suspects = reader.list("suspects")
                    .filterIsInstance<Document>()
                    .mapNotNull { Suspect.fromDocument(it) }
                    .toMutableList(),
                staff = reader.list("staff")
                    .filterIsInstance<String>()
                    .toMutableList(),
                admin = reader.list("admin")
                    .filterIsInstance<String>()
                    .toMutableList(),
                status = InvestigationStatus.valueOf(reader.string("status") ?: return null),
                priority = InvestigationPriority.valueOf(reader.string("priority") ?: return null),
                logs = reader.list("logs")
                    .filterIsInstance<Document>()
                    .mapNotNull { IGLog.fromDocument(it) }
                    .toMutableList()
            )
        }
    }
}
