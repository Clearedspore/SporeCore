package me.clearedSpore.sporeCore.commands.gamemode

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("spectator|gmsp")
@CommandPermission(Perm.SPECTATOR)
@SporeCoreCommand
class SpectatorCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("<player>")
    fun onSpectator(sender: CommandSender, @Optional targets: TargetPlayers?) {

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
            if (sender == target) {
                Logger.log(sender, Perm.LOG, "changed their gamemode to Spectator", false)
            } else {
                Logger.log(sender, Perm.LOG, "changed ${target.name}’s gamemode to Spectator", false)
            }
            sender.sendMessage("You updated ".blue() + target.name.white() + "’s gamemode to Spectator.".blue())
        }
    }
}
