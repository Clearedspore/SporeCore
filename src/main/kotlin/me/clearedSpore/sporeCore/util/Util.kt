package me.clearedSpore.sporeCore.util


object Util {

    fun formatNow(): String =
        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    fun String.noTranslate(): String = this
}