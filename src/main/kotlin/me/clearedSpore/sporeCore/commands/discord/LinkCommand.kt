package me.clearedSpore.sporeCore.commands.discord

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player

@CommandAlias("link")
@CommandPermission(Perm.LINK)
class LinkCommand : BaseCommand() {

    @Default()
    fun onLink(player: Player) {
        val user = UserManager.get(player)
        if (user == null) {
            player.userFail()
            return
        }

        if(user.discordID != null){
            player.sendMessage("You have already linked your account!".red())
            return
        }

        if(DiscordService.hasCode(player.uniqueId)){
            player.sendErrorMessage("You already have a code!")
            return
        }

        val code = DiscordService.generateCode(player.uniqueId)
        player.sendMessage("Your link code is: $code".blue())
        player.sendMessage("Run ".blue() + "/link $code".white() + " in Discord.".blue())
        player.sendMessage("This code expires in &f1 minute".blue() + "!".blue())
    }

}