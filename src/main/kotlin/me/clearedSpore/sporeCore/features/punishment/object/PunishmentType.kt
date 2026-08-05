package me.clearedSpore.sporeCore.features.punishment.`object`


enum class PunishmentType(
    val displayName: String, val pastTense: String, val discordName: String
) {
    BAN("&cBan", "&cBanned", "Ban"),
    TEMPBAN("&cTemp-ban", "&cTemp-banned", "Temp-Ban"),
    KICK("&aKick", "&aKicked", "Kick"),
    MUTE("&eMute", "&eMuted", "Mute"),
    TEMPMUTE("&eTemp-mute", "&eTemp-muted", "Temp-Mute"),
    WARN("&#FF7A00Warn", "&#FF7A00Warned", "Warn"),
    TEMPWARN("&#FF7A00Temp-warn", "&#FF7A00Temp-warned", "Temp-Warn"),

}