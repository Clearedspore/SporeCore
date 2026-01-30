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

@CommandAlias("repair|fix")
@CommandPermission(Perm.REPAIR)
@SporeCoreCommand
class RepairCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("<player>")
    fun onRepair(sender: CommandSender, @Optional targets: TargetPlayers?) {

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }

        val players = resolved.filter {
            sender == it || sender.hasPermission(Perm.REPAIR_OTHERS)
        }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players.")
        }

        var success = 0
        var failed = 0

        players.forEach { player ->
            val item = player.inventory.itemInMainHand

            if (item.type.isAir) {
                failed++
            } else {
                item.durability = 0
                success++
                player.sendMessage("Your item has been repaired.".blue())
            }
        }

        when {
            success > 0 && failed > 0 -> {
                sender.sendMessage(
                    "Successfully repaired the items for $success players and failed for $failed players (no item).".blue()
                )
            }

            success > 0 -> {
                sender.sendMessage(
                    "Successfully repaired the items for $success players.".blue()
                )
            }

            else -> {
                sender.sendMessage(
                    "Failed to repair items for all players (no items in hand).".red()
                )
            }
        }
    }
}
