package me.clearedSpore.sporeCore.features.reports

import com.github.benmanes.caffeine.cache.Caffeine
import me.clearedSpore.sporeAPI.task.Tasks
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Cooldown
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeAPI.util.time.TimeUtil.hours
import me.clearedSpore.sporeAPI.util.time.TimeUtil.toTicks
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.features.reports.`object`.ReportAction
import me.clearedSpore.sporeCore.features.reports.`object`.ReportStatus
import me.clearedSpore.sporeCore.features.reports.`object`.ReportType
import me.clearedSpore.sporeCore.menu.punishment.punish.PunishMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.dizitart.no2.filters.FluentFilter.where
import java.util.UUID
import java.util.concurrent.TimeUnit


object ReportService {

    internal val reportCollection get() = DatabaseManager.getReportCollection()
    private var cleanupTask: BukkitTask? = null

    private val recentReports = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build<UUID, Int>()

    private val sameTargetCooldowns = Caffeine.newBuilder()
        .build<Pair<UUID, UUID>, Long>()


    fun report(reporter: Player, target: OfflinePlayer, reason: String, reportType: ReportType, evidence: String? = null) {
        val report = Report(
            id = UUID.randomUUID().toString(),
            targetUuid = target.uniqueId.toString(),
            targetName = target.name ?: "Unknown",
            targetSuffix = chatService?.getPlayerSuffix(target as? Player)?.translate() ?: "",
            reporterUuid = reporter.uniqueId.toString(),
            reporterName = reporter.name,
            reporterSuffix = chatService?.getPlayerSuffix(reporter)?.translate() ?: "",
            reason = reason,
            evidence = evidence,
            timestamp = System.currentTimeMillis(),
            type = reportType,
            status = ReportStatus.PENDING,
            action = ReportAction.NONE
        )

        reportCollection.insert(report.toDocument())

        UserManager.get(target)?.run {
            this.pastReports.add(report)
            this.save()
        }

        val config = SporeCore.instance.coreConfig.reports
        val evidenceBoolean = if (report.evidence != null) "Yes" else "No"

        Bukkit.getOnlinePlayers().forEach { player ->
            if (!player.hasPermission(Perm.REPORT_STAFF)) return@forEach

            config.newReport.forEach { line ->
                var message = line
                    .replace("%reporter%", "${report.reporterSuffix}${report.reporterName}")
                    .replace("%player%", "${report.targetSuffix}${report.targetName}")
                    .replace("%reason%", report.reason)
                    .replace("%type%", reportType.displayName)
                    .replace("%evidence%", evidenceBoolean)

                if (line.contains("%button%")) {
                    sendReportButtons(player, report.id, report.evidence != null)
                    return@forEach
                }


                player.sendMessage(message.translate())
            }
        }

        reporter.sendSuccessMessage("Thank you for making a report. Staff has been notified!")
        Cooldown.addCooldown("report", reporter.uniqueId)

        sameTargetCooldowns.put(
            reporter.uniqueId to target.uniqueId,
            System.currentTimeMillis()
        )


        val currentCount = recentReports.get(target.uniqueId) { 0 } + 1
        recentReports.put(target.uniqueId, currentCount)
        if (currentCount >= 5 && config.tresHoldMessage.isNotBlank()) {
            val msg = config.tresHoldMessage
                .replace("%player%", target.name ?: "Unknown")
                .replace("%count%", currentCount.toString())
            Bukkit.getOnlinePlayers().filter { it.hasPermission(Perm.REPORT_STAFF) }.forEach { it.sendMessage(msg.translate()) }
        }
    }

    fun sendReportButtons(player: Player, reportId: String, hasEvidence: Boolean) {
        val manageButton = TextComponent("[Click to manage]")
        val colorStr = SporeCore.instance.coreConfig.reports.buttonColor
        val color = ChatColor.of(colorStr) ?: ChatColor.YELLOW
        manageButton.color = color
        manageButton.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reports")
        manageButton.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to manage reports"))

        val components = arrayListOf(manageButton)

        if (hasEvidence) {
            val evidenceButton = TextComponent(" [Click to view Evidence]")
            evidenceButton.color = color
            evidenceButton.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reports evidence $reportId")
            evidenceButton.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to view evidence"))
            components.add(evidenceButton)
        }

        player.spigot().sendMessage(*components.toTypedArray())
    }

    fun completeReport(id: String, player: Player, action: ReportAction, silent: Boolean = false) {
        val reportDoc = reportCollection.find(where("id").eq(id)).firstOrNull() ?: run {
            player.sendErrorMessage("Failed to find report!")
            return
        }

        val report = Report.fromDocument(reportDoc) ?: run {
            player.sendErrorMessage("Failed to parse report!")
            return
        }

        report.status = ReportStatus.COMPLETED
        report.action = action
        report.staffName = player.name
        report.staffUuid = player.uuidStr()
        report.staffSuffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""
        report.silent = silent

        val target = Bukkit.getOfflinePlayer(UUID.fromString(report.targetUuid))

        if (action == ReportAction.ACCEPTED && SporeCore.instance.coreConfig.reports.openPunish) {
            PunishMenu(player, target).open(player)
        }

        if(!silent) {
            val broadcastMsg = if (action == ReportAction.ACCEPTED) {
                SporeCore.instance.coreConfig.reports.reportAccepted
                    .replace("%staff%", "${report.staffSuffix}${player.name}")
                    .replace("%target%", "${report.targetSuffix}${target.name.toString()}")
                    .translate()
            } else {
                SporeCore.instance.coreConfig.reports.reportDenied
                    .replace("%staff%", "${report.staffSuffix}${player.name}")
                    .replace("%target%", "${report.targetSuffix}${target.name.toString()}")
                    .translate()
            }
            Message.broadcastMessageWithPermission(
                broadcastMsg.translate(),
                Perm.REPORT_STAFF
            )
        } else {
            player.sendSuccessMessage("Successfully resolved the report silently!")
        }

        reportCollection.update(where("id").eq(report.id), report.toDocument())

        UserManager.get(UUID.fromString(report.targetUuid))?.run {
            pastReports.removeAll { it.id == report.id }
            pastReports.add(report)
            save()
        }

        if(!silent) {
            val reporter = Bukkit.getOfflinePlayer(UUID.fromString(report.reporterUuid))
            if (reporter.isOnline) {
                Bukkit.getPlayer(reporter.uniqueId)
                    ?.sendMessage(SporeCore.instance.coreConfig.reports.notifyReporter.translate())
            } else {
                UserManager.get(UUID.fromString(report.reporterUuid))?.run {
                    sendMessage(SporeCore.instance.coreConfig.reports.notifyReporter)
                    save()
                } ?: player.sendErrorMessage("Failed to notify reporter! Please contact an administrator")
            }
        }
    }

    fun reOpenReport(id: String, player: Player){
        val reportDoc = reportCollection.find(where("id").eq(id)).firstOrNull() ?: run {
            player.sendErrorMessage("Failed to find report!")
            return
        }

        val report = Report.fromDocument(reportDoc) ?: run {
            player.sendErrorMessage("Failed to parse report!")
            return
        }

        report.status = ReportStatus.RE_OPENED
        reportCollection.update(where("id").eq(report.id), report.toDocument())
        val reporter = Bukkit.getOfflinePlayer(UUID.fromString(report.reporterUuid))


        Message.broadcastMessageWithPermission(
            SporeCore.instance.coreConfig.reports.reportReOpened
                .replace("%staff%", "${report.staffSuffix}${player.name}")
                .replace("%player%", "${report.reporterSuffix}${reporter.name}")
                .translate(),
            Perm.REPORT_ADMIN)

        val msg = SporeCore.instance.coreConfig.reports.reportReOpenedPlayer
            .replace("%staff%", player.name)
            .translate()
        if (reporter.isOnline) {
            Bukkit.getPlayer(reporter.uniqueId)?.sendMessage(msg.translate())
        } else {
            UserManager.get(UUID.fromString(report.reporterUuid))?.run {
                sendMessage(msg)
                save()
            } ?: player.sendErrorMessage("Failed to notify reporter! Please contact an administrator")
        }

    }


    fun startCleanupTask() {
        cleanupTask?.cancel()
        cleanupTask = Tasks.runRepeatedAsync(1.hours.toTicks(), 1.hours.toTicks()) { cleanupReports() }
    }

    fun stopCleanupTask() {
        cleanupTask?.cancel()
        cleanupTask = null
    }

    fun checkSameTargetCooldown(
        reporter: UUID,
        target: UUID
    ): Long? {
        val config = SporeCore.instance.coreConfig.reports
        val cooldownMillis = TimeUtil.parse(config.sameTargetCooldown).millis

        if (cooldownMillis <= 0) return null

        val key = reporter to target
        val lastTime = sameTargetCooldowns.getIfPresent(key) ?: return null

        val elapsed = System.currentTimeMillis() - lastTime
        val remaining = cooldownMillis - elapsed

        return if (remaining > 0) remaining else null
    }



    fun cleanupReports() {
        Logger.infoDB("Clearing reports....")

        val config = SporeCore.instance.coreConfig.reports
        val defaultMaxAge = TimeUtil.parse(config.deletion).millis
        val completedMaxAge = TimeUtil.parse(config.completedDeletion).millis
        val now = System.currentTimeMillis()

        val toRemove = reportCollection.find().toList().mapNotNull { doc ->
            val report = Report.fromDocument(doc) ?: return@mapNotNull null
            val maxAge = if (report.status == ReportStatus.COMPLETED) completedMaxAge else defaultMaxAge
            if (now - report.timestamp >= maxAge) report else null
        }

        if (toRemove.isEmpty()) return

        Logger.infoDB("Removing ${toRemove.size} reports")

        toRemove.forEach {
            reportCollection.remove(where("id").eq(it.id))
        }

        Logger.infoDB("Removed ${toRemove.size} reports")
    }
}