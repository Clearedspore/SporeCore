package me.clearedSpore.sporeCore.commands.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("eco|economy|bal|balance")
@CommandPermission(Perm.ECO)
class EconomyCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    @Syntax("[player]")
    fun onBalance(sender: CommandSender, @Optional targetName: String?) {
        val target = if (sender is Player && targetName == null) sender
        else Bukkit.getOfflinePlayer(targetName ?: return sender.userJoinFail())

        val user = UserManager.get(target.uniqueId)

        if (user == null) {
            sender.userJoinFail()
            return
        }

        val displayName = target.name ?: user.playerName.ifEmpty { "Unknown" }

        val formatted = EconomyService.format(user.balance)
        val suffix = if (Bukkit.getOnlinePlayers().contains(target.player)) chatService?.getPlayerSuffix(target.player)?.translate() ?: "" else ""
        sender.sendMessage("$suffix${displayName}&cb's Balance: ".blue() + formatted.green())
        sender.playSound(Sound.sound(
            net.kyori.adventure.key.Key.key("entity.horse.armor"),
            Sound.Source.PLAYER,
            1f,
            1f),
            Sound.Emitter.self())
    }

    @Subcommand("add")
    @CommandPermission(Perm.ECO_ADMIN)
    @CommandCompletion("@players @payamounts")
    @Syntax("<player> <amount>")
    fun onAdd(sender: CommandSender, targetName: String, amountStr: String) {
        val amount = EconomyService.parseAmount(amountStr)
            ?: return sender.sendMessage("Invalid amount!".red())

        val target = Bukkit.getOfflinePlayer(targetName)
        val user = UserManager.get(target.uniqueId)

        if (user == null) {
            sender.userJoinFail()
            return
        }

        if (amount < 0) {
            sender.sendMessage("Amount must be above 0!".red())
            return
        }

        EconomyService.add(user, amount, "Added by ${sender.name}")

        val suffix = if (Bukkit.getOnlinePlayers().contains(target.player)) chatService?.getPlayerSuffix(target.player)?.translate() ?: "" else ""
        sender.sendMessage("Added ".blue() + EconomyService.format(amount).green() + " to $suffix${user.playerName}&cb.".blue())
        sender.playSound(Sound.sound(
            net.kyori.adventure.key.Key.key("entity.player.levelup"),
            Sound.Source.PLAYER,
            1f,
            1.06f),
            Sound.Emitter.self())
    }

    @Subcommand("remove")
    @CommandPermission(Perm.ECO_ADMIN)
    @CommandCompletion("@players @payamounts")
    @Syntax("<player> <amount>")
    fun onRemove(sender: CommandSender, targetName: String, amountStr: String) {
        val amount = EconomyService.parseAmount(amountStr)
            ?: return sender.sendMessage("Invalid amount!".red())


        val target = Bukkit.getOfflinePlayer(targetName)
        val user = UserManager.get(target.uniqueId)

        if (user == null) {
            sender.userJoinFail()
            return
        }


        if (amount < 0) {
            sender.sendMessage("Amount must be above 0!".red())
            return
        }

        if (user.balance < amount) {
            sender.sendMessage("That player does not have enough money!".red())
            return
        }

        EconomyService.remove(user, amount, "Removed by ${sender.name}")

        val suffix = if (Bukkit.getOnlinePlayers().contains(target.player)) chatService?.getPlayerSuffix(target.player)?.translate() ?: "" else ""
        sender.sendMessage("Removed ".blue() + EconomyService.format(amount).red() + " from $suffix${user.playerName}&cb.".blue())
        sender.playSound(Sound.sound(
            net.kyori.adventure.key.Key.key("entity.player.levelup"),
            Sound.Source.PLAYER,
            1f,
            1.06f),
            Sound.Emitter.self())
    }

    @Subcommand("set")
    @CommandPermission(Perm.ECO_ADMIN)
    @CommandCompletion("@players @payamounts")
    @Syntax("<player> <amount>")
    fun onSet(sender: CommandSender, targetName: String, amountStr: String) {
        val amount = EconomyService.parseAmount(amountStr)
            ?: return sender.sendMessage("Invalid amount!".red())

        val target = Bukkit.getOfflinePlayer(targetName)
        val user = UserManager.get(target.uniqueId)

        if (user == null) {
            sender.userJoinFail()
            return
        }

        if (amount < 0) {
            sender.sendMessage("Amount must be above 0!".red())
            sender.playSound(Sound.sound(
                net.kyori.adventure.key.Key.key("block.note_block.bass"),
                Sound.Source.PLAYER,
                1f,
                2f),
                Sound.Emitter.self())
            return
        }

        EconomyService.set(user, amount, "Set by ${sender.name}")

        val suffix = if (Bukkit.getOnlinePlayers().contains(target.player)) chatService?.getPlayerSuffix(target.player)?.translate() ?: "" else ""
        sender.sendMessage("Set $suffix${user.playerName}&cb's balance to ".blue() + EconomyService.format(amount).green())
        sender.playSound(Sound.sound(
            net.kyori.adventure.key.Key.key("entity.experience_orb.pickup"),
            Sound.Source.PLAYER,
            1f,
            1.3f),
            Sound.Emitter.self())
    }
}
