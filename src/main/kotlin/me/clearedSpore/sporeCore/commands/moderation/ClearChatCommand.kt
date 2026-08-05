package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("clearchat")
@CommandPermission(Perm.CLEAR_CHAT)
class ClearChatCommand : BaseCommand() {


    @Default
    fun onClear(sender: CommandSender) {
        var suffix = if (sender is Player) chatService?.getPlayerSuffix(sender)?.translate() ?: "" else ""
        val lines = PunishmentService.config.settings.clearLines

        for (player in Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(Perm.CLEAR_CHAT_BYPASS)) {
                repeat(lines) {
                    player.sendMessage(" ")
                }
            }
        }

        Logger.log(suffix, sender, Perm.LOG, "cleared the chat", true)
    }
}