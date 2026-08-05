package me.clearedSpore.sporeCore.features.chat.channel

import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.ChannelConfig
import me.clearedSpore.sporeCore.ChatChannelsConfig
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.chat.ChatService
import me.clearedSpore.sporeCore.features.chat.channel.`object`.Channel
import me.clearedSpore.sporeCore.features.setting.impl.ChannelMessagesSetting
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player


object ChatChannelService {

    val chatService = SporeCore.instance.chat
    val config = SporeCore.instance.coreConfig

    fun setChannel(user: User, channel: Channel) {
        Logger.info("Setting user ${user.playerName} channel to ${channel.id}")
        user.channel = channel.id
        UserManager.save(user)
    }

    fun resetChannel(user: User) {
        user.channel = ""
        UserManager.save(user)
    }


    fun sendChannelMessage(player: Player, message: String, channel: Channel) {
        val permission = channel.permission
        val channelMessage = channel.message
        val prefix = chatService?.getPlayerPrefix(player)?.translate() ?: ""
        var suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""

        var format = channelMessage
            .replace("%rankprefix%", prefix)
            .replace("%ranksuffix%", suffix)
            .replace("%player_name%", player.name)
            .replace("%message%", message)

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            format = PlaceholderAPI.setPlaceholders(player, format)
        }

        val translatedTemplate = format.translate()
        val formattedMessage = translatedTemplate.replace("%MESSAGE%", message)

        for (recipient in Bukkit.getOnlinePlayers()) {
            if (recipient.hasPermission(permission) && recipient.hasPermission(Perm.CHANNEL_ALLOW)) {
                val user = UserManager.get(recipient)
                if (user != null && user.getSettingOrDefault(ChannelMessagesSetting())) {
                    recipient.sendMessage(formattedMessage)
                }
            }
        }
    }


    fun getChannelBySymbol(symbol: String): Channel? {
        return getChannels().firstOrNull { it.symbol == symbol }
    }

    fun getChannelByName(channelID: String?): Channel? {
        if (channelID.isNullOrBlank()) return null
        val config = SporeCore.instance.coreConfig.chat.channels.channels
        val lower = channelID.lowercase()
        config[lower]?.let { return it.toChannel() }
        return config.values.firstOrNull { it.id.equals(lower, true) }?.toChannel()
    }


    fun getChannels(): List<Channel> {
        val config = SporeCore.instance.coreConfig.chat.channels
        return config.channels.map { (_, channelConfig) ->
            Channel(
                channelConfig.name,
                channelConfig.id,
                channelConfig.permission,
                channelConfig.prefix,
                channelConfig.message,
                channelConfig.commands,
                channelConfig.symbol
            )
        }
    }


    fun ChannelConfig.toChannel(): Channel {
        return Channel(
            name = name,
            id = id,
            permission = permission,
            prefix = prefix,
            message = message,
            commands = commands,
            symbol = symbol
        )
    }
}