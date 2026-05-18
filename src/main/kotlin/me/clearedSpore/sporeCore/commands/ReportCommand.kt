package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Cooldown
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.extension.PlayerExtension.hasJoinedBefore
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuid
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuidStr
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuid
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.menu.reports.list.ReportListMenu
import me.clearedSpore.sporeCore.menu.reports.report.ReportTypeMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.dizitart.no2.filters.FluentFilter.where

@CommandAlias("report|reports")
class ReportCommand : BaseCommand() {

    @Default()
    @CommandCompletion("@players")
    fun onReport(player: Player, @Optional targetName: String?) {
        if (targetName == null && player.hasPermission(Perm.REPORT_STAFF)) {
            ReportListMenu(player).open(player)
        } else if(targetName != null) {
            val target = Bukkit.getOfflinePlayer(targetName)

            if(target.safeUuid() == player.safeUuid()){
                player.sendErrorMessage("You cannot report yourself!")
                return
            }

            if (Cooldown.isOnCooldown("report", player.uuid())) {
                val timeLeft = TimeUtil.formatDuration(Cooldown.getTimeLeft("report", player.uuid()))
                player.sendErrorMessage("Please wait $timeLeft seconds before doing this again!")
                return
            }

            if (!target.hasJoinedBefore()) {
                player.userJoinFail()
                return
            }

            val remaining = ReportService.checkSameTargetCooldown(
                player.uniqueId,
                target.uniqueId
            )

            if (remaining != null) {
                val time = TimeUtil.formatDuration(remaining, TimeUtil.TimeUnitStyle.SHORT, 2)
                player.sendErrorMessage("You must wait $time before reporting this player again!")
                return
            }

            ReportTypeMenu(target).open(player)
        }
    }

    @Subcommand("cleanup")
    @CommandPermission(Perm.REPORT_ADMIN)
    fun onCleanup(sender: CommandSender) {
        ReportService.cleanupReports()
        sender.sendSuccessMessage("Successfully cleaned up all reports!")
    }

    @Subcommand("evidence")
    @CommandPermission(Perm.REPORT_STAFF)
    @Private
    fun onEvidence(sender: CommandSender, reportID: String) {
        val reportDoc = ReportService.reportCollection.find(where("id").eq(reportID)).firstOrNull() ?: run {
            sender.sendErrorMessage("Failed to find report!")
            return
        }

        val report = Report.fromDocument(reportDoc) ?: run {
            sender.sendErrorMessage("Failed to parse report!")
            return
        }


        sender.sendMessage("&6============= &lReport Evidence&6 =============".translate())
        sender.sendMessage("&lBe careful: don’t click on suspicious links!".red())
        sender.sendMessage("&f".translate())
        sender.sendMessage(report.evidence.toString())
        sender.sendMessage("&f".translate())
        sender.sendMessage("&6==========================================".translate())
        
    }
}