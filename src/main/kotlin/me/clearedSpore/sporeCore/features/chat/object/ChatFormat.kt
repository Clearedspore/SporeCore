package me.clearedSpore.sporeCore.features.chat.`object`

import me.clearedSpore.sporeCore.util.doc.DocReader
import org.dizitart.no2.collection.Document

data class ChatFormat(
    var bold: Boolean = false,
    var italic: Boolean = false,
    var underline: Boolean = false,
    var striketrough: Boolean = false,
    var magic: Boolean = false,
    var none: Boolean = true
) {

    companion object {
        fun fromDocument(doc: Document?): ChatFormat? {
            if (doc == null) return null
            val reader = DocReader(doc)
            val bold = reader.boolean("bold")
            val italic = reader.boolean("italic")
            val underline = reader.boolean("underline")
            val striketrough = reader.boolean("striketrough")
            val magic = reader.boolean("magic")
            val none = reader.boolean("none")
            return ChatFormat(bold, italic, underline, striketrough, magic, none)
        }
    }
}