package me.clearedSpore.sporeCore.features.vanish

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import me.clearedSpore.sporeAPI.exception.LoggedException
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.yellow
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Util.parsePlaceholders
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*


object VanishService {

    var vanishedPlayers: MutableList<UUID> = mutableListOf()

    fun vanish(uuid: UUID) {
        val userPlayer = Bukkit.getPlayer(uuid) ?: return
        val wasInMode = ModeService.isInMode(userPlayer)

        vanishedPlayers.add(uuid)
        val config = SporeCore.instance.coreConfig
        if (!wasInMode && config.joinLeaveMessages.vanish && config.joinLeaveMessages.leave.isNotEmpty()) {
            Bukkit.broadcastMessage(
                config.joinLeaveMessages.leave
                    .translate()
                    .parsePlaceholders(userPlayer)
            )
        }

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(Perm.VANISH_SEE)) continue
            player.hidePlayer(SporeCore.instance, userPlayer)
        }

        if (SporeCore.instance.coreConfig.discord.chat.isNotEmpty() && !wasInMode) {
            val embed = Webhook.Embed()
                .setColor(0xFF0000)
                .setDescription("**${userPlayer.name} left the server**")

            val webhook = Webhook(SporeCore.instance.coreConfig.discord.chat)
                .setProfileURL(DiscordService.getAvatarURL(uuid))
                .setUsername(userPlayer.name)
                .addEmbed(embed)

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


    fun unVanish(uuid: UUID) {
        val userPlayer = Bukkit.getPlayer(uuid) ?: return
        val wasInMode = ModeService.isInMode(userPlayer)

        for (player in Bukkit.getOnlinePlayers()) {
            player.showPlayer(SporeCore.instance, userPlayer)
        }

        vanishedPlayers.remove(uuid)
        val config = SporeCore.instance.coreConfig
        if (!wasInMode && config.joinLeaveMessages.vanish && config.joinLeaveMessages.join.isNotEmpty()) {
            Bukkit.broadcastMessage(
                config.joinLeaveMessages.join
                    .translate()
                    .parsePlaceholders(userPlayer))
        }

        if (SporeCore.instance.coreConfig.discord.chat.isNotEmpty() && !wasInMode) {
            val embed = Webhook.Embed()
                .setColor(0x00FF00)
                .setDescription("**${userPlayer.name} joined the server**")

            val webhook = Webhook(SporeCore.instance.coreConfig.discord.chat)
                .setProfileURL(DiscordService.getAvatarURL(uuid))
                .setUsername(userPlayer.name)
                .addEmbed(embed)

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

    fun toggle(uuid: UUID) {
        if (isVanished(uuid)) {
            unVanish(uuid)
        } else {
            vanish(uuid)
        }
    }

    fun isVanished(uuid: UUID): Boolean {
        return vanishedPlayers.contains(uuid)
    }

}