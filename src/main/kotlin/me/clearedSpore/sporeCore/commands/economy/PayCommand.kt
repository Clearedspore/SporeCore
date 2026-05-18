package me.clearedSpore.sporeCore.commands.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Cooldown
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuid
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.eco.PaymentCooldownService
import me.clearedSpore.sporeCore.features.message.Message
import me.clearedSpore.sporeCore.features.message.MessageType
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

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

        if (!PaymentCooldownService.canPay(sender.uniqueId)) {
            val timeLeft = TimeUtil.formatDuration(Cooldown.getTimeLeft("report", sender.uuid()))
            sender.sendErrorMessage("You must wait $timeLeft seconds before paying again.")
            return
        }

        PaymentCooldownService.onPayment(sender.uniqueId)
        EconomyService.remove(senderUser, amount, "Paid to ${targetUser.playerName}")
        EconomyService.add(targetUser, amount, "Received from ${sender.name}", false)

        val formattedAmount = EconomyService.format(amount)
        sender.sendMessage("You paid ${targetUser.playerName} ".blue() + formattedAmount.green())

        val message = Message(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            MessageType.PAYMENT,
            "You received ".blue() + formattedAmount.green() + " from ${sender.name}.".blue(),
            sender.uuid(),
            false
        )

        targetUser.queueMessage(message)
    }
}
