package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.exception.LoggedException
import me.clearedSpore.sporeAPI.util.CC
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.AutoListener
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService
import me.clearedSpore.sporeCore.features.chat.color.ChatColorService
import me.clearedSpore.sporeCore.features.chat.color.`object`.ChatColor
import me.clearedSpore.sporeCore.features.chat.`object`.ChatFormat
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.setting.impl.ChatEnabledSetting
import me.clearedSpore.sporeCore.features.setting.impl.MentionOption
import me.clearedSpore.sporeCore.features.setting.impl.MentionTitleSetting
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ActionBar.actionBar
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Util.noTranslate
import me.clip.placeholderapi.PlaceholderAPI
import net.dv8tion.jda.api.managers.WebhookManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

@AutoListener
class ChatListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        // If it is cancelled before it checks for this listener return
        if(event.isCancelled) return

        if (ChatInputService.has(player)) {
            event.isCancelled = true
            val msg = event.message
            if (msg.equals("cancel", true)) {
                ChatInputService.cancel(player)
                player.sendErrorMessage("Cancelled procedure.")
            } else {
                Task.run {
                    ChatInputService.consume(player, msg)
                }
            }
            return
        }

        val senderUser = UserManager.get(player) ?: run {
            player.userFail()
            event.isCancelled = true
            return
        }

        if (!senderUser.getSettingOrDefault(ChatEnabledSetting())) {
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

            if (channel != null && player.hasPermission(channel.permission)) {
                val cleanedMessage = msg.substring(1)
                ChatChannelService.sendChannelMessage(player, cleanedMessage, channel)
                event.isCancelled = true
                return
            }
        }

        val userChannelId = senderUser.channel
        if (!userChannelId.isNullOrEmpty() && SporeCore.instance.coreConfig.features.channels) {
            val channel = ChatChannelService.getChannelByName(userChannelId)
            if (channel != null) {
                if (!player.hasPermission(channel.permission) || !player.hasPermission(Perm.CHANNEL_ALLOW)) {
                    player.sendErrorMessage("You don't have permission to type in this channel!")
                    player.sendErrorMessage("Your channel has been set to global!")
                    UserManager.get(player)?.let { ChatChannelService.resetChannel(it) }
                    event.isCancelled = true
                    return
                }
                ChatChannelService.sendChannelMessage(player, event.message, channel)
                event.isCancelled = true
                return
            } else {
                Logger.info("User channel is empty or invalid, sending to global chat")
            }
        }


        if (ModeService.isInMode(player)) {
            val mode = ModeService.getPlayerMode(player)!!
            if (!mode.chat) {
                event.isCancelled = true
                player.sendMessage("You can't talk while in ${mode.name} mode!".red())
                return
            }
        }

        val toRemove = event.recipients.filter {
            val recipientUser = UserManager.get(it)
            recipientUser == null ||
                    (!player.hasPermission(Perm.CHAT_BYPASS) && !recipientUser.getSettingOrDefault(ChatEnabledSetting()))
        }
        Task.run { event.recipients.removeAll(toRemove) }

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

        val chatColor = if (config.chat.chatColor.enabled) ChatColorService.getColor(senderUser) else null
        val appliedColor = chatColor?.colorString ?: ""
        val chatFormat = senderUser.chatFormat?.toCodeString() ?: ""

        message = "$appliedColor$chatFormat$message".translate()

        val mentionsConfig = config.chat.mentions
        val mentionedPlayers = mutableSetOf<Player>()

        if (mentionsConfig.enabled) {
            for (target in Bukkit.getOnlinePlayers()) {
                if (target.uniqueId == player.uniqueId) continue

                val nameRegex = Regex(Regex.escape(target.name), RegexOption.IGNORE_CASE)
                if (nameRegex.containsMatchIn(message)) {
                    mentionedPlayers.add(target)
                    message = message.replace(nameRegex) {
                        "&a${it.value}$appliedColor"
                    }
                }
            }
        }

        val prefix = chatService?.getPlayerPrefix(player)?.translate() ?: ""
        var suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""
        val chatFormatConfig = config.chat.formatting

        if (VanishService.isVanished(senderUser.uuid) && chatFormatConfig.hideVanishSuffix) {
            suffix = ""
        }

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

        mentionedPlayers.forEach { target ->
            val targetUser = UserManager.get(target) ?: return@forEach

            val titleEnabled = targetUser.getSettingOrDefault(MentionTitleSetting()) == MentionOption.TITLE_ENABLED
            val soundEnabled = targetUser.getSettingOrDefault(MentionTitleSetting()) == MentionOption.SOUND_ENABLED
            val allEnabled = targetUser.getSettingOrDefault(MentionTitleSetting()) == MentionOption.TITLE_AND_SOUND

            val actionbar = config.chat.mentions.actionBar

            if (actionbar.isNotBlank()) {
                val formatted = actionbar
                    .replace("%player%", player.name)

                target.actionBar("mention", formatted.translate())
            }

            if (titleEnabled || allEnabled) {
                val title = mentionsConfig.title
                val subTitle = mentionsConfig.subTitle
                if (!title.isNullOrBlank() || !subTitle.isNullOrBlank()) {
                    target.sendTitle(
                        title.replace("%player%", player.name).translate(),
                        subTitle.replace("%player%", player.name).translate(),
                        10,
                        40,
                        10
                    )
                }
            }

            if (soundEnabled || allEnabled) {
                val soundName = mentionsConfig.sound
                if (soundName.isNotBlank()) {
                    runCatching {
                        target.playSound(
                            target.location,
                            org.bukkit.Sound.valueOf(soundName),
                            1f,
                            1f
                        )
                    }
                }
            }
        }

        if (config.logs.chat) {
            LogsService.addLog(player.uuidStr(), message.noTranslate(), LogType.CHAT)
        }

        if(chatFormatConfig.enabled) {
            event.recipients.forEach { it.sendMessage(formattedMessage) }
            event.isCancelled = true
            Bukkit.getConsoleSender().sendMessage(formattedMessage)
        }

        event.message = formattedMessage

        if(config.discord.chat.isNotEmpty()){
            val dcMessage = org.bukkit.ChatColor.stripColor(message)!!.replace("@", "")
            val webhook = Webhook(config.discord.chat)
                .setMessage(dcMessage)
                .setUsername(player.name)
                .setProfileURL(DiscordService.getAvatarURL(player.uniqueId))

            try {
                webhook.send()
            } catch (ex: Exception) {
                throw LoggedException(
                    userMessage = "Failed to send message to Discord.",
                    internalMessage = "Failed to send message to Discord",
                    channel = LoggedException.Channel.GENERAL,
                    developerOnly = false,
                    cause = ex
                ).also { it.log() }
            }
        }
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
