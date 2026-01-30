package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

@CommandAlias("disposal|trash")
@CommandPermission(Perm.TRASH)
@SporeCoreCommand
class TrashCommand : BaseCommand() {

    @Default()
    @CommandCompletion("@players")
    @Syntax("[player]")
    fun onTrash(sender: CommandSender, @Optional targetName: String?) {
        val target: Player? = when {
            sender is Player && targetName == null -> sender
            targetName != null -> Bukkit.getPlayer(targetName)
            else -> null
        }

        if (target == null) {
            sender.sendMessage("That player is not online!".red())
            return
        }

        if (sender != target && !sender.hasPermission(Perm.TRASH_OTHERS)) {
            sender.sendMessage("You don't have permission to open the trash menu for other players".red())
            return
        }

        val inventory: Inventory = Bukkit.createInventory(target, 54, "Trash")
        target.openInventory(inventory)

        if (sender != target) {
            sender.sendMessage("You have opened the trash menu for ${target.name}".blue())
        }
    }
}