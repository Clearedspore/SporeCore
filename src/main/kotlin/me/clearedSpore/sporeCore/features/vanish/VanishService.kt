package me.clearedSpore.sporeCore.features.vanish

import me.clearedSpore.sporeAPI.exception.LoggedException
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import java.util.*


object VanishService {

    var vanishedPlayers: MutableList<UUID> = mutableListOf()

    fun vanish(uuid: UUID) {
        val userPlayer = Bukkit.getPlayer(uuid) ?: return

        vanishedPlayers.add(uuid)

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(Perm.VANISH_SEE)) continue
            player.hidePlayer(SporeCore.instance, userPlayer)
        }

        if (SporeCore.instance.coreConfig.discord.chat.isNotEmpty()) {
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

        for (player in Bukkit.getOnlinePlayers()) {
            player.showPlayer(SporeCore.instance, userPlayer)
        }

        vanishedPlayers.remove(uuid)

        if (SporeCore.instance.coreConfig.discord.chat.isNotEmpty()) {
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