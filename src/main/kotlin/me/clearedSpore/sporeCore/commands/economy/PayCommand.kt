package me.clearedSpore.sporeCore.commands.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("pay")
@CommandPermission(Perm.ECO)
class PayCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players @payamounts")
    @Syntax("<player> <amount>")
    fun onPay(sender: Player, targetName: String, amountStr: String) {
        if (!SporeCore.instance.coreConfig.economy.paying) {
            sender.sendErrorMessage("Paying other players is disabled.")
            return
        }

        val amount = EconomyService.parseAmount(amountStr) ?: run {
            sender.sendMessage("Invalid amount!".red())
            return
        }

        val senderUser = UserManager.get(sender.uniqueId)
        if (senderUser == null) {
            sender.userJoinFail()
            return
        }

        val targetOffline = Bukkit.getOfflinePlayer(targetName)
        val targetUser = UserManager.get(targetOffline.uniqueId)
        if (targetUser == null) {
            sender.userJoinFail()
            return
        }

        if (senderUser.balance < amount) {
            sender.sendErrorMessage("You do not have enough money.")
            return
        }

        EconomyService.remove(senderUser, amount, "Paid to ${targetUser.playerName}")
        EconomyService.add(targetUser, amount, "Received from ${sender.name}", false)

        val formattedAmount = EconomyService.format(amount)
        sender.sendMessage("You paid ${targetUser.playerName} ".blue() + formattedAmount.green())

        val onlinePlayer = Bukkit.getPlayer(targetUser.uuid)
        if (onlinePlayer != null && onlinePlayer.isOnline) {
            onlinePlayer.sendMessage("You received ".blue() + formattedAmount.green() + " from ${sender.name}.".blue())
        } else {
            targetUser.queuePayment(sender.name, amount)
        }
    }
}
