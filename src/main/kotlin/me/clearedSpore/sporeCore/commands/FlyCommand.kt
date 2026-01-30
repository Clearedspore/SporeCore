package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("fly|togglefly")
@CommandPermission(Perm.FLIGHT)
@SporeCoreCommand
class FlyCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    fun onFly(sender: CommandSender, @Optional targets: TargetPlayers?) {

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }

        val players = resolved.filter {
            sender == it || sender.hasPermission(Perm.FLIGHT_OTHERS)
        }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players.")
        }

        players.forEach {
            it.allowFlight = !it.allowFlight
            it.sendMessage(
                "Your flight has been ${if (it.allowFlight) "enabled" else "disabled"}.".blue()
            )
        }

        sender.sendMessage(
            if (players.size == 1)
                "Flight toggled for ${players.first().name}.".blue()
            else
                "Flight toggled for ${players.size} players.".blue()
        )
    }
}
