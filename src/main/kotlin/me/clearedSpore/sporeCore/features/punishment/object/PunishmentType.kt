package me.clearedSpore.sporeCore.features.punishment.`object`


enum class PunishmentType(
    val displayName: String, val pastTense: String
) {
    BAN("Ban", "Banned"),
    TEMPBAN("Temp-ban", "Temp-banned"),
    KICK("Kick", "Kicked"),
    MUTE("Mute", "Muted"),
    TEMPMUTE("Temp-mute", "Temp-muted"),
    WARN("Warn", "Warned"),
    TEMPWARN("Temp-warn", "Temp-warned")

}