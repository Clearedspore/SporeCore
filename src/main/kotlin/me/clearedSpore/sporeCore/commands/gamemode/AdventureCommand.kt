package me.clearedSpore.sporeCore.commands.gamemode

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.util.Perm
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("adventure|gma")
@CommandPermission(Perm.ADVENTURE)
@SporeCoreCommand
class AdventureCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("<player>")
    fun onAdventure(sender: CommandSender, @Optional targets: TargetPlayers?) {
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
            target.gameMode = GameMode.ADVENTURE
            var targetSuffix = chatService?.getPlayerSuffix(target)?.translate() ?: ""
            if (sender == target) {
                Logger.log(suffix, sender, Perm.LOG, "changed their gamemode to Adventure", false)
                sender.sendMessage("Your gamemode has been updated to Adventure".blue())
            } else {
                Logger.log(suffix, sender, Perm.LOG, "changed $targetSuffix${target.name}&r&f’s gamemode to Adventure", false)
                sender.sendMessage("You updated ".blue() + targetSuffix + target.name + "’s gamemode to Adventure.".blue())
                target.player?.sendMessage("Your gamemode has been updated to Adventure".blue())
            }
        }
    }
}
