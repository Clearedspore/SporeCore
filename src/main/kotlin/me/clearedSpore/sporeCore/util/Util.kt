package me.clearedSpore.sporeCore.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.Locale


object Util {

    fun formatNow(): String =
        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    fun String.noTranslate(): String = this

    fun String.parsePlaceholders(player: Player): String {
        return if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderAPI.setPlaceholders(player, this)
        } else {
            this
        }
    }

    fun String.niceName(): String {
        val string = this
        val words = string.replace("_", " ").lowercase(Locale.getDefault()).split(" ")
        return words.joinToString(" ") { it.replaceFirstChar { c -> c.titlecaseChar() } }
    }

    fun wrapWithColors(input: String, lineLength: Int): List<String> {
        val words = input.split(" ")
        val lines = mutableListOf<String>()

        var currentLine = StringBuilder()
        var lastColors = ""

        fun extractColors(text: String): String {
            val regex = Regex("(?i)&[0-9A-FK-OR]")
            return regex.findAll(text).joinToString("") { it.value }
        }

        words.forEach { word ->
            if (currentLine.length + word.length + 1 > lineLength) {
                lines.add((lastColors + currentLine.toString()).trim())
                lastColors = extractColors(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add((lastColors + currentLine.toString()).trim())
        }

        return lines
    }

}