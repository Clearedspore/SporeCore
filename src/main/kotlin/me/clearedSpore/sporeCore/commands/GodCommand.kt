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

@CommandAlias("invulnerable|god")
@CommandPermission(Perm.GOD)
@SporeCoreCommand
class GodCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("[player]")
    fun onGodMode(sender: CommandSender, @Optional targets: TargetPlayers?) {
        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }

        val players = resolved.filter {
            sender == it || sender.hasPermission(Perm.GOD_OTHERS)
        }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players.")
        }

        players.forEach {
            it.isInvulnerable = !it.isInvulnerable
            it.sendMessage(
                "Your godmode has been ${if (it.isInvulnerable) "enabled" else "disabled"}.".blue()
            )
        }

        sender.sendMessage(
            if (players.size == 1)
                "GodMode toggled for ${players.first().name}.".blue()
            else
                "GodMode toggled for ${players.size} players.".blue()
        )
    }
}
