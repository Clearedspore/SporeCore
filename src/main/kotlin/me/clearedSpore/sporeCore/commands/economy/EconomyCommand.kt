package me.clearedSpore.sporeCore.commands.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
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
        else Bukkit.getOfflinePlayer(targetName ?: return sender.userFail())

        val user = UserManager.get(target.uniqueId)

        if(user == null){
            sender.userFail()
            return
        }

        val displayName = target.name ?: user.playerName.ifEmpty { "Unknown" }

        val formatted = EconomyService.format(user.balance)
        sender.sendMessage("${displayName}'s Balance: ".blue() + formatted.green())
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

        if(user == null){
            sender.userFail()
            return
        }

        EconomyService.add(user, amount, "Added by ${sender.name}")

        sender.sendMessage("Added ".blue() + EconomyService.format(amount).green() + " to ${user.playerName}.".blue())
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

        if(user == null){
            sender.userFail()
            return
        }

        EconomyService.remove(user, amount, "Removed by ${sender.name}")

        sender.sendMessage("Removed ".blue() + EconomyService.format(amount).red() + " from ${user.playerName}.".blue())
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

        if(user == null){
            sender.userFail()
            return
        }

        EconomyService.set(user, amount, "Set by ${sender.name}")

        sender.sendMessage("Set ${user.playerName}'s balance to ".blue() + EconomyService.format(amount).green())
    }
}
