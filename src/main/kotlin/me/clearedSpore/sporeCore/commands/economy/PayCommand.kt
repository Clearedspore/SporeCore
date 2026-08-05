package me.clearedSpore.sporeCore.commands.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Cooldown
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuid
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.eco.PaymentCooldownService
import me.clearedSpore.sporeCore.features.message.Message
import me.clearedSpore.sporeCore.features.message.MessageType
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.collections.contains

@CommandAlias("pay")
@CommandPermission(Perm.ECO)
class PayCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players @payamounts")
    @Syntax("<player> <amount>")
    fun onPay(sender: Player, targetName: String, amountStr: String) {
        if (!SporeCore.instance.coreConfig.economy.paying) {
            sender.sendErrorMessage("This server has disabled paying other players!")
            return
        }

        val amount = EconomyService.parseAmount(amountStr) ?: run {
            sender.sendMessage("The amount you have specified is invalid!".red())
            sender.playSound(Sound.sound(
                net.kyori.adventure.key.Key.key("block.note_block.bass"),
                Sound.Source.PLAYER,
                1f,
                2f),
                Sound.Emitter.self())
            return
        }

        val senderUser = UserManager.get(sender.uniqueId)
        if (senderUser == null) {
            sender.userJoinFail()
            sender.playSound(Sound.sound(
                net.kyori.adventure.key.Key.key("block.note_block.bass"),
                Sound.Source.PLAYER,
                1f,
                2f),
                Sound.Emitter.self())
            return
        }

        val targetOffline = Bukkit.getOfflinePlayer(targetName)
        val targetUser = UserManager.get(targetOffline.uniqueId)
        if (targetUser == null) {
            sender.userJoinFail()
            sender.playSound(Sound.sound(
                net.kyori.adventure.key.Key.key("block.note_block.bass"),
                Sound.Source.PLAYER,
                1f,
                2f),
                Sound.Emitter.self())
            return
        }

        if (senderUser.balance < amount) {
            sender.sendErrorMessage("You do not have the balance specified.")
            sender.playSound(Sound.sound(
                net.kyori.adventure.key.Key.key("block.note_block.bass"),
                Sound.Source.PLAYER,
                1f,
                2f),
                Sound.Emitter.self())
            return
        }

        if (!PaymentCooldownService.canPay(sender.uniqueId)) {
            val timeLeft = TimeUtil.formatDuration(Cooldown.getTimeLeft("report", sender.uuid()))
            sender.sendErrorMessage("You must wait $timeLeft seconds before paying again.")
            sender.playSound(Sound.sound(
                net.kyori.adventure.key.Key.key("block.note_block.bass"),
                Sound.Source.PLAYER,
                1f,
                2f),
                Sound.Emitter.self())
            return
        }

        PaymentCooldownService.onPayment(sender.uniqueId)
        val senderSuffix = chatService?.getPlayerSuffix(sender.player)?.translate() ?: ""
        val targetSuffix = if (Bukkit.getOnlinePlayers().contains(targetUser.player)) chatService?.getPlayerSuffix(targetUser.player)?.translate() ?: "" else ""
        EconomyService.remove(senderUser, amount, "Paid to ${targetUser.playerName}")
        EconomyService.add(targetUser, amount, "Received from ${sender.name}", false)

        val formattedAmount = EconomyService.format(amount)
        sender.sendMessage("You paid $targetSuffix${targetUser.playerName}&r ".blue() + formattedAmount.green())


        val message = Message(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            MessageType.PAYMENT,
            "You received ".blue() + formattedAmount.green() + " from $senderSuffix${sender.name}&r&cb.".blue(),
            sender.uuid(),
            false
        )

        targetUser.queueMessage(message)
        targetUser.player?.playSound(Sound.sound(
            net.kyori.adventure.key.Key.key("entity.item.pickup"),
            Sound.Source.PLAYER,
            0.3f,
            2f),
            Sound.Emitter.self())
        TODO("Add sounds to most commands - QoL Change")
    }
}
