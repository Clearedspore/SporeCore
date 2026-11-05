package me.clearedSpore.sporeCore.menu.reports.list.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.bold
import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.features.reports.`object`.ReportStatus
import me.clearedSpore.sporeCore.menu.investigation.list.InvestigationListMenu
import me.clearedSpore.sporeCore.menu.punishment.history.HistoryMenu
import me.clearedSpore.sporeCore.menu.reports.list.ReportListMenu
import me.clearedSpore.sporeCore.menu.reports.list.resolve.ResolveMenu
import me.clearedSpore.sporeAPI.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.button.TextButton
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import kotlin.math.sin


class ReportItem(
    val report: Report,
    val viewer: Player
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.BOOK)
            .setName("${report.targetSuffix}${report.targetName}")
            .addLoreLine("Reporter: &f${report.reporterSuffix}${report.reporterName}".blue())
            .addLoreLine("Suspect: &f${report.targetSuffix}${report.targetName}".blue())
            .addLoreLine("Reason: &f${report.reason} &7(${report.type.displayName.capitalizeFirstLetter()})".blue())

        val age = System.currentTimeMillis() - report.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)

        item.addLoreLine("Time: &f$timeAgo ago".blue())

        val evidenceBoolean = if (report.evidence != null) "Yes" else "No"

        item.addLoreLine("Evidence: &f$evidenceBoolean".blue())

        if ((report.status == ReportStatus.COMPLETED ||
                    report.status == ReportStatus.RE_OPENED)
            && viewer.hasPermission(Perm.REPORT_ADMIN)
        ) {

            item.addLoreLine("Result: &f${report.action.displayName}".blue())
            item.addLoreLine("Staff member: &f${report.staffSuffix}${report.staffName}".blue())
            item.addLoreLine("Silent: &f${report.silent.toString().lowercase().capitalizeFirstLetter()}".blue())
        }

        item.addLoreLine("&f".translate())

        if (report.evidence != null) {
            item.addUsageLine(ClickType.RIGHT, "to view evidence")
        }

        if (report.status != ReportStatus.COMPLETED) {

            item.addUsageLine(ClickType.LEFT, "to resolve")
            if (viewer.hasPermission(Perm.HISTORY_OTHERS) && SporeCore.instance.coreConfig.features.punishments) {
                item.addUsageLine(ClickType.MIDDLE, "view punishments history")
            }
            if (viewer.hasPermission(Perm.WHOIS)) {
                item.addUsageLine(ClickType.SHIFT_LEFT, "to view user info")
            }
        } else if (report.status == ReportStatus.COMPLETED && viewer.hasPermission(Perm.REPORT_ADMIN)) {
            item.addUsageLine(ClickType.LEFT, "to re-open the report")
        }

        if (IGService.isStaff(viewer)
            || viewer.hasPermission(Perm.INVESTIGATION_ADMIN)
            && SporeCore.instance.coreConfig.features.investigation
        ) {
            item.addUsageLine(ClickType.SHIFT_RIGHT, "add to an investigation")
        }

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {

        if (clickType == ClickType.SHIFT_RIGHT &&
            (IGService.isStaff(viewer)
                    || viewer.hasPermission(Perm.INVESTIGATION_ADMIN))
            && SporeCore.instance.coreConfig.features.investigation
        ) {
            InvestigationListMenu(viewer, report.id, true).open(viewer)
            return
        }

        if (clickType.isRightClick && report.evidence != null) {
            clicker.closeInventory()
            clicker.sendMessage("&6============= &lReport Evidence&6 =============".translate())
            clicker.sendMessage("&lBe careful: don’t click on suspicious links!".red())
            clicker.sendMessage("&f".translate())
            clicker.sendMessage(report.evidence.toString())
            clicker.sendMessage("&f".translate())
            clicker.sendMessage("&6==========================================".translate())
            return
        }

        if (report.status != ReportStatus.COMPLETED) {

            if (clickType == ClickType.SHIFT_LEFT && clicker.hasPermission(Perm.WHOIS)) {
                clicker.performCommand("whois " + report.targetName)
                clicker.closeInventory()
                return
            }

            if (clickType == ClickType.MIDDLE &&
                clicker.hasPermission(Perm.HISTORY_OTHERS) &&
                SporeCore.instance.coreConfig.features.punishments
            ) {
                val offlineTarget = Bukkit.getOfflinePlayer(report.targetName)
                HistoryMenu(clicker, offlineTarget).open(clicker)
                return
            }

            if (clickType == ClickType.LEFT) {
                ResolveMenu(clicker, report).open(clicker)
                return
            }

        } else if (report.status == ReportStatus.COMPLETED && viewer.hasPermission(Perm.REPORT_ADMIN)) {
            if (clickType == ClickType.LEFT) {
                ReportService.reOpenReport(report.id, clicker)
                ReportListMenu(clicker, ReportStatus.RE_OPENED).open(clicker)
            }
        }
    }
}