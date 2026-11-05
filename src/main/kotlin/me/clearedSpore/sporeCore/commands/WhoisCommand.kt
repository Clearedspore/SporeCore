package me.clearedSpore.sporeCore.commands

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.currency.CurrencySystemService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.stats.StatService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import kotlin.reflect.typeOf

@CommandAlias("whois")
@CommandPermission(Perm.WHOIS)
@RegisterCommand
class WhoisCommand : SporeCommand() {

    @Default()
    @CommandCompletion("@players")
    @Syntax("<player>")
    fun onWhois(sender: CommandSender, targetName: String) {
        val target = Bukkit.getOfflinePlayer(targetName)
        val user = UserManager.get(target)

        if (!target.hasPlayedBefore() || user == null) {
            sender.userFail()
            return
        }

        val playtime = TimeUtil.formatDuration(StatService.getTotalPlaytime(user))
        val weeklyPlaytime = TimeUtil.formatDuration(StatService.getWeeklyPlaytime(user))
        val lastJoin = StatService.getLastJoin(user)
        val firstJoin = StatService.getFirstJoin(user)
        val firstServerIP = user.firstServerIP
        val lastServerIP = user.lastServerIP

        val currencyName = CurrencySystemService.currencyName
        val credits = CurrencySystemService.getBalance(user)
        val formatted = CurrencySystemService.format(credits)

        val currencyEnabled = SporeCore.instance.coreConfig.features.currency.enabled
        val punishmentsEnabled = SporeCore.instance.coreConfig.features.punishments

        sender.sendMessage("-----------------------------")
        sender.sendMessage("Who is $targetName?".blue())
        sender.sendMessage("")

        if (currencyEnabled) {
            sender.sendMessage("$currencyName: ".white() + "$formatted")
            sender.sendMessage("")
        }

        sender.sendMessage("Playtime: ".white() + "$playtime".blue())
        sender.sendMessage("Weekly Playtime: ".white() + "$weeklyPlaytime".blue())
        sender.sendMessage("Last Join: ".white() + lastJoin.blue())
        sender.sendMessage("First Join: ".white() + firstJoin.blue())
        sender.sendMessage("")
        if (firstServerIP != null) sender.sendMessage("First server IP: ".white() + "$firstServerIP".blue())
        if (lastServerIP != null) sender.sendMessage("Last server IP: ".white() + "$lastServerIP".blue())
        if (lastServerIP != null || firstServerIP != null) sender.sendMessage("")

        if (punishmentsEnabled) {
            val activePunishments = user.getActivePunishments()
            sender.sendMessage("Punishments: ".white())
            if (activePunishments.isEmpty()) {
                sender.sendMessage("None".blue())
            } else {
                activePunishments.forEach { pun ->
                    if (pun.type == PunishmentType.TEMPWARN || pun.type == PunishmentType.WARN) return@forEach

                    val type = pun.type.displayName
                    val reason = pun.reason
                    val staff = pun.getPunisher()!!.playerName
                    val expires = pun.getDurationFormatted()

                    sender.sendMessage("- $type by $staff: $reason ($expires)".blue())
                }
            }
            sender.sendMessage("")
        }

        sender.sendMessage("-----------------------------")

    }
}