package me.clearedSpore.sporeCore.commands.currency

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.currency.CurrencySystemService
import me.clearedSpore.sporeCore.currency.menu.main.CurrencyMainMenu
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.text.ifEmpty

@CommandAlias("%currencyalias")
class CurrencyCommand : BaseCommand() {

    private val service = CurrencySystemService

    @Default
    @CommandPermission(Perm.CURRENCY_GET)
    fun onDefault(sender: CommandSender, @Optional target: OfflinePlayer?) {
        if (target == null && sender !is Player) {
            sender.sendMessage("You must specify a player.".red())
            return
        }

        val targetPlayer: OfflinePlayer = when {
            target != null -> {
                if (!sender.hasPermission(Perm.CURRENCY_GET_OTHERS)) {
                    sender.sendMessage("You do not have permission to view other players' balances.".red())
                    return
                }
                target
            }
            sender is Player -> sender
            else -> {
                sender.sendMessage("You must specify a player.".red())
                return
            }
        }

        if (!targetPlayer.hasPlayedBefore()) {
            sender.userJoinFail()
            return
        }

        val user = UserManager.get(targetPlayer)
        if (user == null) {
            sender.sendMessage("User data not found.".red())
            return
        }

        val balance = service.getBalance(user)
        val formatted = CurrencySystemService.format(balance)

        if (sender.name.equals(targetPlayer.name, ignoreCase = true)) {
            sender.sendMessage("You have $formatted.".blue())
        } else {
            sender.sendMessage("${targetPlayer.name} has $formatted.".blue())
        }
    }


    @Subcommand("set")
    @CommandCompletion("@players")
    @Syntax("<player> <amount>")
    @CommandPermission(Perm.CURRENCY_ADMIN)
    fun onSet(sender: CommandSender, target: OfflinePlayer, amount: Double, reason: String) {
        if (!target.hasPlayedBefore()) {
            sender.userJoinFail()
            return
        }

        val user = UserManager.get(target)
        if (user == null) {
            sender.userFail()
            return
        }

        val formatted = CurrencySystemService.format(amount)
        service.setBalance(sender, user, amount, reason)
        sender.sendMessage("Set ${target.name}'s balance to $formatted.".blue())
    }

    @Subcommand("get")
    @CommandCompletion("@players")
    @Syntax("<player>")
    @CommandPermission(Perm.CURRENCY_ADMIN)
    fun onGet(sender: CommandSender, target: OfflinePlayer) {
        if (!target.hasPlayedBefore()) {
            sender.userJoinFail()
            return
        }

        val user = UserManager.get(target)
        if (user == null) {
            sender.sendMessage("User data not found.".red())
            return
        }


        val balance = service.getBalance(user)
        val formatted = CurrencySystemService.format(balance)
        sender.sendMessage("${target.name} has $formatted.".blue())
    }

    @Subcommand("add")
    @CommandCompletion("@players")
    @Syntax("<player> <amount>")
    @CommandPermission(Perm.CURRENCY_ADMIN)
    fun onAdd(sender: CommandSender, target: OfflinePlayer, amount: Double, reason: String) {
        if (!target.hasPlayedBefore()) {
            sender.userJoinFail()
            return
        }

        val user = UserManager.get(target)
        if (user == null) {
            sender.userFail()
            return
        }

        val formatted = CurrencySystemService.format(amount)

        service.addBalance(sender, user, amount, reason)
        sender.sendMessage("Added $formatted".blue() + " to ${target.name}.".blue())
    }

    @Subcommand("topbought")
    @CommandCompletion("true")
    @CommandPermission(Perm.CURRENCY_TOP)
    fun onTopBought(sender: CommandSender, @Optional lastMonth: Boolean?) {
        val lastMonthOnly = lastMonth ?: false
        val topLimit = 10

        val topList = CurrencySystemService.topBoughtPackages(lastMonthOnly, topLimit)

        if (topList.isEmpty()) {
            sender.sendMessage("No package purchase data found.".blue())
            return
        }

        val title = if (lastMonthOnly) "Top $topLimit Packages (Last 30 days)" else "Top $topLimit Packages (All time)"
        sender.sendMessage("=== $title ===".blue())

        topList.forEachIndexed { index, (packageName, count) ->
            sender.sendMessage("${index + 1}. $packageName - $count purchases".blue())
        }
    }

    @Subcommand("topspent")
    @CommandCompletion("true")
    @CommandPermission(Perm.CURRENCY_TOP)
    fun onTopSpent(sender: CommandSender, @Optional lastMonth: Boolean?) {
        val lastMonthOnly = lastMonth ?: false
        val topLimit = 10

        CurrencySystemService.topSpenders(lastMonthOnly, topLimit).thenAccept { topList ->
            if (topList.isEmpty()) {
                sender.sendMessage("No spending data found.".blue())
                return@thenAccept
            }

            val title = if (lastMonthOnly) "Top $topLimit Spenders (Last 30 days)" else "Top $topLimit Spenders (All time)"
            sender.sendMessage("=== $title ===".blue())

            topList.forEachIndexed { index, (player, spent) ->
                val name = player.name ?: "Unknown"
                val formatted = CurrencySystemService.format(spent)
                sender.sendMessage("${index + 1}. $name - $formatted spent".blue())
            }
        }
    }


    @Subcommand("logs|transactions")
    @CommandCompletion("@players @Range:1-10")
    @Syntax("<player> [page]")
    @CommandPermission(Perm.CURRENCY_ADMIN)
    fun onLogs(sender: CommandSender, targetName: String, @Optional page: Int?){
        val logPage = page ?: 1
        val user = UserManager.get(Bukkit.getOfflinePlayer(targetName).uniqueId)

        if(user == null){
            sender.userJoinFail()
            return
        }


        user.getCreditLogs(logPage, 10).thenAccept { logs ->


            if (logs.isEmpty()) {
                sender.sendMessage("No logs found.".red())
                return@thenAccept
            }

            val currency = CurrencySystemService.config.currencySettings.pluralName
            val displayName = user.playerName.ifEmpty { targetName }
            sender.sendMessage("=== $currency Logs for $displayName (Page $logPage) ===".blue())
            logs.forEach { sender.sendMessage(it.translate()) }
        }
    }

    @Subcommand("top")
    @CommandPermission(Perm.CURRENCY_TOP)
    fun onTop(sender: CommandSender) {
        val topLimit = 10

        service.topCredits(topLimit).thenAccept { topList ->
            if (topList.isEmpty()) {
                sender.sendMessage("No credit data found.".blue())
                return@thenAccept
            }

            val currencyName = service.config.currencySettings.pluralName
            sender.sendMessage("=== Top $topLimit $currencyName ===".blue())

            topList.forEachIndexed { index, (player, credits) ->
                val formatted = service.format(credits)
                val name = player.name ?: "Unknown"
                sender.sendMessage("${index + 1}. $name - $formatted".blue())
            }
        }
    }

    @Subcommand("remove")
    @CommandCompletion("@players")
    @Syntax("<player> <amount>")
    @CommandPermission(Perm.CURRENCY_ADMIN)
    fun onRemove(sender: CommandSender, target: OfflinePlayer, amount: Double, reason: String) {
        if (!target.hasPlayedBefore()) {
            sender.userJoinFail()
            return
        }

        val user = UserManager.get(target)
        if (user == null) {
            sender.userFail()
            return
        }

        val formatted = CurrencySystemService.format(amount)

        service.removeBalance(sender, user, amount, reason)
        sender.sendMessage("Removed $formatted".blue() + " from ${target.name}.".blue())
    }

    @Subcommand("shop")
    fun onShop(player: Player) {
        CurrencyMainMenu(player).open(player)
    }
}