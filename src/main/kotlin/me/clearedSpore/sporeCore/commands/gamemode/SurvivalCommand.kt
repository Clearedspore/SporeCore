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

@CommandAlias("survival|gms")
@CommandPermission(Perm.SURVIVAL)
@RegisterCommand
class SurvivalCommand : SporeCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("<player>")
    fun onSurvival(sender: CommandSender, @Optional targets: TargetPlayers?) {
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
            target.gameMode = GameMode.SURVIVAL
            var targetSuffix = chatService?.getPlayerSuffix(target)?.translate() ?: ""
            if (sender == target) {
                Logger.log(suffix, sender, Perm.LOG, "&rchanged their gamemode to Survival", false)
                sender.sendMessage("Your gamemode has been updated to Survival".blue())
            } else {
                Logger.log(suffix, sender, Perm.LOG, "&rchanged $targetSuffix${target.name}&r&f’s gamemode to Survival", false)
                sender.sendMessage("You updated ".blue() + targetSuffix + target.name + "’s gamemode to Survival.".blue())
            }
        }
    }
}
