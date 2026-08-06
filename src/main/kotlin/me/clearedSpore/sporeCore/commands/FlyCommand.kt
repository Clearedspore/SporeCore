package me.clearedSpore.sporeCore.commands

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("fly|togglefly")
@CommandPermission(Perm.FLIGHT)
@RegisterCommand
class FlyCommand : SporeCommand() {

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

        var suffix = chatService?.getPlayerSuffix(players.first().player)?.translate() ?: ""

        sender.sendMessage(
            if (players.size == 1 && players.first().name != sender.name)
                "Flight toggled for $suffix${players.first().name}".translate() + ".".blue()
            else
                "Flight toggled for $suffix${players.size}".translate() + " players.".blue()
        )
    }
}
