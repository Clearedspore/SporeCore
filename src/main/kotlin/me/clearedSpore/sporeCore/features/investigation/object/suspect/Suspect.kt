package me.clearedSpore.sporeCore.features.investigation.`object`.suspect

import me.clearedSpore.sporeCore.features.investigation.`object`.note.Note
import me.clearedSpore.sporeCore.util.doc.DocReader
import me.clearedSpore.sporeCore.util.doc.DocWriter
import org.bukkit.Bukkit
import org.dizitart.no2.collection.Document
import org.dizitart.no2.repository.annotations.Id
import java.util.UUID

data class Suspect(
    @Id var id: String,
    var uuid: String,
    var description: String,
    var addedBy: String,
    var timestamp: Long
){


    fun getSuspectName() : String? {
        val player = Bukkit.getOfflinePlayer(UUID.fromString(uuid))
        return player.name
    }

    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("uuid", uuid)
        .put("description", description)
        .put("addedBy", addedBy)
        .put("timestamp", timestamp)
        .build()

    companion object {
        fun fromDocument(doc: Document): Suspect? {
            val reader = DocReader(doc)

            return Suspect(
                id = reader.string("id") ?: return null,
                uuid = reader.string("uuid") ?: return null,
                description = reader.string("description") ?: return null,
                addedBy = reader.string("addedBy") ?: return null,
                timestamp = reader.long("timestamp") ?: return null
            )
        }

    }

}
