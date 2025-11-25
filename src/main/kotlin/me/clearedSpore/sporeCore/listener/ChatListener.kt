package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService
import me.clearedSpore.sporeCore.features.chat.color.ChatColorService
import me.clearedSpore.sporeCore.features.chat.`object`.ChatFormat
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.user.settings.Setting
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Tasks
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listener {

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val senderUser = UserManager.get(player) ?: run {
            player.userFail()
            event.isCancelled = true
            return
        }


        if (!senderUser.isSettingEnabled(Setting.CHAT_ENABLED) && SporeCore.instance.coreConfig.features.settings) {
            player.sendErrorMessage("You can't send messages while having chat disabled!")
            event.isCancelled = true
            return
        }

        if (senderUser.isMuted() && SporeCore.instance.coreConfig.features.punishments) {
            val mute = senderUser.getActivePunishment(PunishmentType.MUTE) ?: run {
                event.isCancelled = true
                return
            }

            val msg = PunishmentService.getMessage(mute.type)
            val formatted = PunishmentService.buildMessage(msg, mute)
            player.sendMessage(formatted)

            event.isCancelled = true
            event.recipients.clear()

            if (PunishmentService.config.settings.notifyTry) {
                val tryMsgTemplate = when (mute.type) {
                    PunishmentType.MUTE -> PunishmentService.config.logs.tryMute
                    PunishmentType.TEMPMUTE -> PunishmentService.config.logs.tryTempMute
                    else -> null
                }

                tryMsgTemplate?.let {
                    val broadcastMsg = PunishmentService.buildTryMessage(it, mute, senderUser)
                    Message.broadcastMessageWithPermission(broadcastMsg, Perm.PUNISH_LOG)
                }
            }

            return
        }

        val msg = event.message
        if (msg.isNotEmpty() && SporeCore.instance.coreConfig.features.channels) {
            val symbol = msg.substring(0, 1)
            val channel = ChatChannelService.getChannelBySymbol(symbol)

            if (channel != null) {
                if (player.hasPermission(channel.permission)) {
                    val cleanedMessage = msg.substring(1)
                    ChatChannelService.sendChannelMessage(player, cleanedMessage, channel)
                    event.isCancelled = true
                    return
                }
            }
        }

        val userChannelId = senderUser.channel
        if (userChannelId != null && SporeCore.instance.coreConfig.features.channels) {
            val channel = ChatChannelService.getChannelByName(userChannelId)
            if (channel != null) {
                if (!player.hasPermission(channel.permission) || !player.hasPermission(Perm.CHANNEL_ALLOW)) {
                    player.sendErrorMessage("You don't have permission to type in this channel!")
                    player.sendErrorMessage("Your channel has been set to global!")
                    val user = UserManager.get(player)
                    if (user != null) {
                        ChatChannelService.resetChannel(user)
                    }
                    event.isCancelled = true
                    return
                }

                ChatChannelService.sendChannelMessage(player, event.message, channel)
                event.isCancelled = true
                return
            } else {
                Logger.error("Failed to find channel with id '$userChannelId'")
            }
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


        val chatColor = if (config.chat.chatColor.enabled)
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
