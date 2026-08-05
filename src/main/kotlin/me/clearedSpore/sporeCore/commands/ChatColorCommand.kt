package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.chat.color.ChatColorService
import me.clearedSpore.sporeCore.menu.chatcolor.ChatColorMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("cc|chatcolor")
@CommandPermission(Perm.CHATCOLOR)
class ChatColorCommand : BaseCommand() {

    @Default()
    fun onChatColor(player: Player) {
        ChatColorMenu(player).open(player)
    }


    @Subcommand("set")
    @CommandCompletion("@players @colors")
    @CommandPermission(Perm.ADMIN)
    @Syntax("<player> <color>")
    fun onSetColor(sender: CommandSender, targetName: String, colorStr: String) {
        val target = Bukkit.getOfflinePlayer(targetName)
        val targetSuffix = if (Bukkit.getOnlinePlayers().contains(target)) chatService?.getPlayerSuffix(target.player)?.translate() ?: "" else ""

        val user = UserManager.get(target)

        if (user == null) {
            sender.userJoinFail()
            return
        }

        val color = ChatColorService.getColorByKey(colorStr)

        if (color == null) {
            sender.sendMessage("That color does not exist!".red())
            return
        }

        ChatColorService.setColor(user, color)
        sender.sendMessage("You have set $targetSuffix$targetName".blue() + "'s chatcolor to $colorStr".blue())

    }
}