package me.clearedSpore.sporeCore.features.chat.color.`object`

import me.clearedSpore.sporeCore.util.doc.DocReader
import org.dizitart.no2.collection.Document


data class ChatColor(
    var name: String,
    var colorString: String,
    var material: String
) {

    companion object {
        fun fromDocument(doc: Document?): ChatColor? {
            if (doc == null) return null
            val reader = DocReader(doc)
            val name = reader.string("name") ?: return null
            val colorString = reader.string("colorString") ?: return null
            val material = reader.string("material") ?: return null
            return ChatColor(name, colorString, material)
        }
    }
}
