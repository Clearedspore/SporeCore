package me.clearedSpore.sporeCore.commands.inventory

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("inventorymanager|invmanager|invm")
@CommandPermission(Perm.ADMIN)
class InventoryManagerCommand : BaseCommand() {

    @Subcommand("remove")
    @CommandPermission(Perm.INV_DELETE)
    fun onRemove(sender: CommandSender, targetName: String, id: String) {
        val target = Bukkit.getOfflinePlayer(targetName)
        val uuid = target.uniqueId
        val user = UserManager.get(uuid)

        if (user == null) {
            sender.userFail()
            return
        }

        val inventory = user.pendingInventories.find { it == id }

        if (inventory != null) {
            user.pendingInventories.remove(inventory)
            UserManager.save(user)
            sender.sendMessage("Inventory $id removed!".blue())
        } else {
            sender.sendMessage("No pending inventory with ID $id found.".red())
        }
    }

    @Subcommand("clearall player")
    @CommandCompletion("@players")
    @CommandPermission(Perm.INV_ADMIN)
    fun onClear(sender: CommandSender, targetName: String) {
        val target = Bukkit.getOfflinePlayer(targetName)

        val inventories = InventoryManager.getInventoriesOf(target)

        if (inventories.isEmpty()) {
            sender.sendMessage("No inventories found!".red())
            return
        }

        var removed = 0
        val start = System.currentTimeMillis()
        sender.sendMessage("Clearing inventories async...".blue())
        Task.runAsync {
            inventories.forEach { inventory ->
                InventoryManager.removeInventory(inventory.id)
                removed++
            }
        }

        val end = System.currentTimeMillis()
        val total = end - start
        sender.sendMessage("Took $total ms to clear $removed inventories!".blue())
    }

    @Subcommand("clearall all")
    @CommandPermission(Perm.INV_ADMIN)
    fun onClearAll(sender: CommandSender, @Optional confirm: String?) {
        val inventories = InventoryManager.inventoryCollection

        sender.sendMessage("Finding inventories..".blue())
        val amount = inventories.size()

        sender.sendMessage("Found $amount inventories".blue())

        if (confirm.isNullOrEmpty() || confirm != "confirm") {
            sender.sendMessage("")
            sender.sendMessage("            &lWARNING              ".red())
            sender.sendMessage("Are you sure you want to do this?".blue())
            sender.sendMessage("You are about to clear $amount inventories".blue())
            sender.sendMessage("This action cannot be undone!".red())
            sender.sendMessage("Run &f/invmanager clearall confirm".blue() + " to confirm".blue())
            sender.sendMessage("")
            return
        }

        val start = System.currentTimeMillis()
        sender.sendMessage("Clearing inventories async...".blue())

        Task.runAsync {
            InventoryManager.clearAll()
        }

        val end = System.currentTimeMillis()
        val total = end - start
        sender.sendMessage("Took $total ms to clear $amount inventories!".blue())
    }


}