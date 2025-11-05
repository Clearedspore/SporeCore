package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.chat.ChatColorService
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
    fun onChatColor(player: Player){
        ChatColorMenu(player).open(player)
    }


    @Subcommand("set")
    @CommandCompletion("@players @colors")
    @CommandPermission(Perm.ADMIN)
    @Syntax("<player> <color>")
    fun onSetColor(sender: CommandSender, targetName: String, colorStr: String){
        val target = Bukkit.getOfflinePlayer(targetName)

        val user = UserManager.get(target)

        if(user == null){
            sender.userJoinFail()
            return
        }

        val color = ChatColorService.getColorByKey(colorStr)

        if(color == null){
            sender.sendMessage("That color does not exist!".red())
            return
        }

        ChatColorService.setColor(user, color)
        sender.sendMessage("You have set $targetName's chatcolor to $colorStr".blue())

    }
}