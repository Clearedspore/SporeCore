package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("repairall|fixall")
@CommandPermission(Perm.REPAIRALL)
@SporeCoreCommand
class RepairAllCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("<targets>")
    fun onRepair(sender: CommandSender, @Optional targets: TargetPlayers?) {

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player.")
        }


        if (resolved.isEmpty()) {
            sender.sendMessage("No valid players selected.".red())
            return
        }

        var success = 0
        var failed = 0

        resolved.forEach { player ->
            var repaired = 0

            (player.inventory.contents + player.inventory.extraContents + arrayOf(player.inventory.itemInOffHand)).forEach { item ->
                if (item != null && item.type != Material.AIR) {
                    item.durability = 0
                    repaired++
                }
            }

            player.inventory.armorContents.forEach { armor ->
                if (armor != null && armor.type != Material.AIR) {
                    armor.durability = 0
                    repaired++
                }
            }

            if (repaired > 0) {
                success++
                player.sendMessage("All your items have been repaired.".blue())
            } else {
                failed++
            }
        }

        when {
            success > 0 && failed > 0 ->
                sender.sendMessage(
                    "Successfully repaired items for $success players and failed for $failed players (no items).".blue()
                )

            success > 0 ->
                sender.sendMessage(
                    "Successfully repaired items for $success players.".blue()
                )

            else ->
                sender.sendMessage(
                    "Failed to repair items for all players (no items).".red()
                )
        }
    }
}
