package me.clearedSpore.sporeCore.commands.channel

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService
import me.clearedSpore.sporeCore.features.chat.channel.`object`.Channel
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ActionBar.actionBar
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player

@CommandAlias("%channelalias")
@CommandPermission(Perm.CHANNEL_ALLOW)
class ChannelCommand(val channel: Channel) : BaseCommand() {

    @Default
    fun onChannel(player: Player, vararg messageParts: String) {

        if (!player.hasPermission(channel.permission)) {
            player.sendMessage("You don't have permission to type in this channel".red())
            player.actionBar("noperm", "No permission!")
            return
        }

        val user = UserManager.get(player)
        if (user == null) {
            player.userFail()
            return
        }

        if (messageParts.isEmpty()) {
            if (user.channel == channel.id) {
                ChatChannelService.resetChannel(user)
                player.actionBar("toggledChannel", "Disabled ${channel.prefix} chat".blue())
            } else {
                ChatChannelService.setChannel(user, channel)
                player.actionBar("toggledChannel", "Enabled ${channel.prefix} chat".blue())
            }
        } else {
            val message = messageParts.joinToString(" ")
            ChatChannelService.sendChannelMessage(player, message, channel)
        }
    }
}
