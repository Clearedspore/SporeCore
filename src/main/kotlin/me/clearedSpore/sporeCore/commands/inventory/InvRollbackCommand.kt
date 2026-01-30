package me.clearedSpore.sporeCore.commands.inventory

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.menu.invrollback.InvRollBackMenu
import me.clearedSpore.sporeCore.menu.invrollback.claim.ClaimMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("invrollback|invrestore|restoreinv")
@CommandPermission(Perm.INV_ROLLBACK)
class InvRollbackCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    fun onRollback(player: Player, @Optional @Name("target") targetName: String?) {

        if (targetName.isNullOrEmpty()) {
            val user = UserManager.get(player)
            if (user == null) {
                player.userFail()
                return
            }

            user.pendingInventories.removeIf { InventoryManager.getInventory(it) == null }
            UserManager.save(user)

            if (user.pendingInventories.isEmpty()) {
                player.sendErrorMessage("You don't have any inventories you can claim!")
                return
            }

            ClaimMenu(player).open(player)
        } else {
            val target = Bukkit.getOfflinePlayer(targetName)

            if (!target.hasPlayedBefore()) {
                player.userJoinFail()
                return
            }

            InvRollBackMenu(player, target).open(player)
        }
    }
}