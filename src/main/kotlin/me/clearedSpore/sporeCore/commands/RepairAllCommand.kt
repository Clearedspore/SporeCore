package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.extension.Extensions.isNullOrAir
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("repairall|fixall")
@CommandPermission(Perm.REPAIRALL)
class RepairAllCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    fun onRepair(sender: CommandSender, @Optional targetName: String?) {

        val target: Player? = when {
            sender is Player && targetName == null -> sender
            targetName != null -> Bukkit.getPlayer(targetName)
            else -> null
        }

        if (target == null) {
            sender.sendMessage("That player is not online!".red())
            return
        }

        if (sender != target && !sender.hasPermission(Perm.REPAIRALL_OTHERS)) {
            sender.sendMessage("You don't have permission to repair other players' items.".red())
            return
        }

        var repairedCount = 0

        (target.inventory.contents + target.inventory.extraContents + arrayOf(target.inventory.itemInOffHand)).forEach { item ->
            if (item != null && item.type != Material.AIR) {
                item.durability = 0
                repairedCount++
            }
        }

        target.inventory.armorContents.forEach { armor ->
            if (armor != null && armor.type != Material.AIR) {
                armor.durability = 0
                repairedCount++
            }
        }

        if (repairedCount > 0) {
            if (sender == target) {
                sender.sendMessage("All your items have been repaired.".blue())
            } else {
                sender.sendMessage("You have repaired all of ${target.name}'s items.".blue())
                target.sendMessage("All your items have been repaired.".blue())
            }
        } else {
            if (sender == target) {
                sender.sendMessage("You have no items to repair.".red())
            } else {
                sender.sendMessage("${target.name} has no items to repair.".red())
            }
        }
    }

    @Subcommand("*")
    @CommandPermission(Perm.REPAIRALL_OTHERS)
    fun onAll(sender: CommandSender) {
        var successful = 0
        var failed = 0

        Bukkit.getOnlinePlayers().forEach { player ->
            var repairedCount = 0

            (player.inventory.contents + player.inventory.extraContents + arrayOf(player.inventory.itemInOffHand)).forEach { item ->
                if (item != null && item.type != Material.AIR) {
                    item.durability = 0
                    repairedCount++
                }
            }

            player.inventory.armorContents.forEach { armor ->
                if (armor != null && armor.type != Material.AIR) {
                    armor.durability = 0
                    repairedCount++
                }
            }

            if (repairedCount > 0) {
                successful++
                player.sendMessage("All your items have been repaired.".blue())
            } else {
                failed++
            }
        }

        sender.sendMessage("Successfully repaired $successful player(s).".blue())
        if (failed > 0) {
            sender.sendMessage("Failed to repair $failed player(s) (no items to repair).".red())
        }
    }
}
