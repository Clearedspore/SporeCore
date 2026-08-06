package me.clearedSpore.sporeCore.commands.gamemode

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("spectator|gmsp")
@CommandPermission(Perm.SPECTATOR)
@RegisterCommand
class SpectatorCommand : SporeCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("<player>")
    fun onSpectator(sender: CommandSender, @Optional targets: TargetPlayers?) {
        var suffix = if (sender is Player) chatService?.getPlayerSuffix(sender)?.translate() ?: "" else ""

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> {
                sender.sendMessage("You must specify a player when running this command from console.".red())
                return
            }
        }

        val players = resolved.filter { sender == it || sender.hasPermission(Perm.GAMEMODE_OTHERS) }

        if (players.isEmpty()) {
            sender.sendMessage("No valid players.".red())
            return
        }

        players.forEach { target ->
            target.gameMode = GameMode.SPECTATOR
            var targetSuffix = chatService?.getPlayerSuffix(target)?.translate() ?: ""
            if (sender == target) {
                Logger.log(suffix, sender, Perm.LOG, "&rchanged their gamemode to Spectator", false)
                sender.sendMessage("Your gamemode has been updated to Spectator".blue())
            } else {
                Logger.log(suffix, sender, Perm.LOG, "&rchanged $targetSuffix${target.name}&r&f’s gamemode to Spectator", false)
                sender.sendMessage("You updated ".blue() + targetSuffix + target.name + "&r’s gamemode to Spectator.".blue())
            }
        }
    }
}
