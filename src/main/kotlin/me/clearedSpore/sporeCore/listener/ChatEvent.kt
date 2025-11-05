package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.chat.ChatColorService
import me.clearedSpore.sporeCore.features.chat.`object`.ChatFormat
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.user.settings.Setting
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Tasks
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatEvent : Listener {

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val senderUser = UserManager.get(player) ?: run {
            player.userFail()
            event.isCancelled = true
            return
        }


        if (!senderUser.isSettingEnabled(Setting.CHAT_ENABLED)) {
            player.sendErrorMessage("You can't send messages while having chat disabled!")
            event.isCancelled = true
            return
        }


        val toRemove = event.recipients.filter { recipient ->
            val recipientUser = UserManager.get(recipient)
            recipientUser == null ||
                    (!player.hasPermission(Perm.CHAT_BYPASS) &&
                            !recipientUser.isSettingEnabled(Setting.CHAT_ENABLED))
        }
        Tasks.run { event.recipients.removeAll(toRemove) }

        val config = SporeCore.instance.coreConfig
        val chatService = SporeCore.instance.chat

        var message = event.message
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(player, message)
        }


        if (player.hasPermission(Perm.COLORED_CHAT) && config.features.coloredChat) {
            message = message.translate()
        } else {
            message = message
                .replace(Regex("&[0-9a-fk-orA-FK-OR]"), "")
                .replace(Regex("<#[a-fA-F0-9]{6}>"), "")
                .replace(Regex("<[a-zA-Z_]+>"), "")
        }


        val chatColor = if (config.features.chatColor && config.chat.chatColor.enabled)
            ChatColorService.getColor(senderUser)
        else null
        val appliedColor = chatColor?.colorString ?: ""


        val chatFormat = senderUser.chatFormat?.toCodeString() ?: ""


        message = "$appliedColor$chatFormat$message".translate()

        val prefix = chatService?.getPlayerPrefix(player)?.translate() ?: ""
        val suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""

        val chatFormatConfig = config.chat.formatting
        val formattedMessage = if (chatFormatConfig.enabled) {
            var format = chatFormatConfig.format
                .replace("%rankprefix%", prefix)
                .replace("%ranksuffix%", suffix)
                .replace("%player_name%", player.name)
                .replace("%message%", message)

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                format = PlaceholderAPI.setPlaceholders(player, format)
            }

            format.translate()
        } else {
            "$prefix${player.name}$suffix: $message".translate()
        }


        event.isCancelled = true
        event.recipients.forEach { it.sendMessage(formattedMessage) }
    }

    private fun ChatFormat.toCodeString(): String {
        if (none) return ""
        val codes = StringBuilder()
        if (bold) codes.append("&l")
        if (italic) codes.append("&o")
        if (underline) codes.append("&n")
        if (striketrough) codes.append("&m")
        if (magic) codes.append("&k")
        return codes.toString()
    }
}
