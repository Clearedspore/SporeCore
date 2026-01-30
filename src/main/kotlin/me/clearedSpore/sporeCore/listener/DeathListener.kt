package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.exception.LoggedException
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.StringUtil.firstPart
import me.clearedSpore.sporeAPI.util.StringUtil.hasFlag
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.AutoListener
import me.clearedSpore.sporeCore.features.discord.DiscordService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent

@AutoListener
class DeathListener : Listener {

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player
        val config = SporeCore.instance.coreConfig
        val deathMessage = event.deathMessage

        if (config.discord.chat.isNotEmpty()) {
            val embed = Webhook.Embed()
                .setColor(0x00FF00)
                .setDescription("**${deathMessage}**")

            val webhook = Webhook(config.discord.chat)
                .setProfileURL(DiscordService.getAvatarURL(player.uniqueId))
                .setUsername(player.name)
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

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        val config = SporeCore.instance.coreConfig
        val kitService = SporeCore.instance.kitService

        if (config.kits.deathKit.isNotEmpty()) {
            val kitName = config.kits.deathKit.firstPart()
            val shouldClear = config.kits.deathKit.hasFlag("clear")
            val kits = kitService.getAllKits()

            val kit = kits.find { it.name.equals(kitName, ignoreCase = true) }

            if (kit == null) {
                Logger.error("Failed to give death kit to ${player.name}")
                return
            }

            if (shouldClear) {
                player.inventory.clear()
            }

            kitService.giveKit(player, kitName)
        }
    }

}