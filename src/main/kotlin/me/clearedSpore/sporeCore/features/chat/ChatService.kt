package me.clearedSpore.sporeCore.features.chat

import me.clearedSpore.sporeAPI.util.CC.translate
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object ChatService {

    private fun getChat(): Chat? {
        val rsp = Bukkit.getServer().servicesManager.getRegistration(Chat::class.java)
        return rsp?.provider
    }


    fun getPrefix(player: Player): String {
        val chat = getChat() ?: return ""
        return chat.getPlayerPrefix(player).translate()
    }


    fun getSuffix(player: Player): String {
        val chat = getChat() ?: return ""
        return chat.getPlayerSuffix(player).translate()
    }


    fun getPlayerPrefixColor(player: Player): String {
        val prefix = getPrefix(player)
        return extractLastColorCode(prefix)
    }


    fun getPlayerSuffixColor(player: Player): String {
        val suffix = getSuffix(player)
        return extractLastColorCode(suffix)
    }


    fun getPrefixColor(player: Player): String {
        return getPlayerPrefixColor(player).translate()
    }


    fun getSuffixColor(player: Player): String {
        return getPlayerSuffixColor(player).translate()
    }


    private fun extractLastColorCode(text: String): String {
        if (text.isEmpty()) return "&7"


        val hexRegex = Regex("&#[A-Fa-f0-9]{6}")
        val lastHex = hexRegex.findAll(text).lastOrNull()?.value
        if (lastHex != null) return lastHex


        val legacyRegex = Regex("&[0-9a-fk-or]")
        val lastLegacy = legacyRegex.findAll(text).lastOrNull()?.value
        return lastLegacy ?: "&7"
    }
}
