package me.clearedSpore.sporeCore.commands.discord

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Confirmation
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player

@CommandAlias("unlink")
@CommandPermission(Perm.LINK)
class UnLinkCommand : BaseCommand() {

    @Default()
    fun onUnLink(player: Player) {
        val user = UserManager.get(player)
        if (user == null) {
            player.userFail()
            return
        }

        if(user.discordID == null){
            player.sendMessage("You have not linked your account yet!".red())
            player.sendMessage("Run".red() + " /link".white() + " to link".red())
            return
        }

        if(Confirmation.isPlayerPending(player.uniqueId)) {
            user.discordID == null
            player.sendSuccessMessage("Successfully unlinked your account!")
            Confirmation.removePlayer(player.uniqueId)
        } else {
            Confirmation.addPlayer(player.uniqueId)
        }
    }
}