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

@CommandAlias("repair|fix")
@CommandPermission(Perm.REPAIR)
class RepairCommand : BaseCommand() {

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

        if (sender != target && !sender.hasPermission(Perm.REPAIR_OTHERS)) {
            sender.sendMessage("You don't have permission to repair other players' items.".red())
            return
        }

        val item = target.inventory.itemInMainHand
        if (item.isNullOrAir()) {
            if(sender == target){
                sender.sendMessage("You must be holding an item!".red())
            } else {
                sender.sendMessage("${target.name} must be holding an item!".red())
            }
            return
        }

        item.durability = 0

        if (sender == target) {
            sender.sendMessage("Your item has been repaired.".blue())
        } else {
            sender.sendMessage("You have repaired ${target.name}'s item.".blue())
            target.sendMessage("Your item has been repaired.".blue())
        }
    }

    @Subcommand("*")
    @CommandPermission(Perm.REPAIR_OTHERS)
    fun onAll(sender: CommandSender) {
        var successful = 0
        var failed = 0

        Bukkit.getOnlinePlayers().forEach { player ->
            val item = player.inventory.itemInMainHand
            if (item != null && item.type != Material.AIR) {
                item.durability = 0
                successful++
                player.sendMessage("Your item has been repaired.".blue())
            } else {
                failed++
            }
        }

        sender.sendMessage("Successfully repaired $successful item(s).".blue())
        if (failed > 0) {
            sender.sendMessage("Failed to repair $failed item(s) (no item in hand).".red())
        }
    }
}
