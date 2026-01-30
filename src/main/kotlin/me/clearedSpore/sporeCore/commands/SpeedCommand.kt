package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("speed")
@CommandPermission(Perm.SPEED)
@SporeCoreCommand
class SpeedCommand : BaseCommand() {

    @Default
    @CommandCompletion("@range:1-10 @players")
    @Syntax("<speed> [targets]")
    fun onSpeed(sender: CommandSender, speed: Float, @Optional targets: TargetPlayers?) {
        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }

        val players = resolved.filter {
            sender == it || sender.hasPermission(Perm.SPEED_OTHERS)
        }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players.")
        }

        if (speed !in 1.0..10.0) {
            sender.sendMessage("Amount must be between 1 - 10!".red())
            return
        }

        players.forEach { target ->
            val bukkitSpeed = speed / 10f
            target.flySpeed = bukkitSpeed
            target.walkSpeed = bukkitSpeed
        }

        sender.sendMessage(
            when {
                players.size == 1 && players.first() == sender -> "Your speed has been set to $speed.".blue()
                players.size == 1 -> "Speed set to $speed for ${players.first().name}.".blue()
                else -> "Speed set to $speed for ${players.size} players.".blue()
            }
        )
    }

    @Subcommand("reset")
    @Syntax("[targets]")
    @CommandCompletion("@targets")
    fun onReset(sender: CommandSender, @Optional targets: TargetPlayers?) {
        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }

        val players = resolved.filter {
            sender == it || sender.hasPermission(Perm.SPEED_OTHERS)
        }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players.")
        }

        players.forEach { player ->
            player.flySpeed = 0.1f
            player.walkSpeed = 0.2f
            if (player != sender) {
                player.sendMessage("Your speed has been reset to default.".blue())
            }
        }

        sender.sendMessage(
            if (players.size == 1)
                if (players.first() == sender)
                    "Your speed has been reset to default.".blue()
                else
                    "You reset ${players.first().name}'s speed to default.".blue()
            else
                "You reset speed for ${players.size} players.".blue()
        )
    }
}