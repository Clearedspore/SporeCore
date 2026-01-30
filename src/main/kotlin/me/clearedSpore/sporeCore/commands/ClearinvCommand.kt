package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("clearinv|clearinventory|ci|clear")
@SporeCoreCommand
class ClearinvCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    fun onClear(sender: CommandSender, @Optional targets: TargetPlayers?) {

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }

        val players = resolved.filter {
            sender == it || sender.hasPermission(Perm.CLEAR_OTHERS)
        }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players to clear.")
        }

        players.forEach {
            it.inventory.clear()
            it.sendMessage("Your inventory has been cleared.".blue())
        }

        sender.sendMessage(
            if (players.size == 1)
                "You cleared ${players.first().name}'s inventory.".blue()
            else
                "You cleared ${players.size} players' inventories.".blue()
        )
    }
}


