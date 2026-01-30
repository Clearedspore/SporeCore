package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("feed")
@CommandPermission(Perm.FEED)
@SporeCoreCommand
class FeedCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    fun onFeed(sender: CommandSender, @Optional targets: TargetPlayers?) {

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }

        val players = resolved.filter {
            sender == it || sender.hasPermission(Perm.FEED_OTHERS)
        }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players to feed.")
        }

        players.forEach {
            it.foodLevel = 20
            it.saturation = 20f
        }

        sender.sendMessage(
            if (players.size == 1)
                "You fed ${players.first().name}.".blue()
            else
                "You fed ${players.size} players.".blue()
        )

        Logger.log(
            sender,
            Perm.LOG,
            "fed ${players.size} player(s)",
            false
        )
    }
}
