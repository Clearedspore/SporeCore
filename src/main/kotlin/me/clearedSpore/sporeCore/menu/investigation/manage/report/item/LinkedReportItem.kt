package me.clearedSpore.sporeCore.menu.investigation.manage.report.item

import com.sk89q.worldedit.util.report.ReportList
import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.features.reports.`object`.ReportStatus
import me.clearedSpore.sporeCore.menu.investigation.manage.report.LinkedReportMenu
import me.clearedSpore.sporeCore.menu.reports.list.ReportListMenu
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class LinkedReportItem(
    val investigation: Investigation,
    val report: Report,
    val viewer: Player
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.BOOK)
            .setName(report.id.blue())
            .addLoreLine("")
            .addLoreLine("Reporter: &f${report.reporterName}".blue())
            .addLoreLine("Suspect: &f${report.targetName}".blue())
            .addLoreLine("Reason: &f${report.reason} &7(${report.type.displayName.capitalizeFirstLetter()})".blue())
        val age = System.currentTimeMillis() - report.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)

        item.addLoreLine("Time: &f$timeAgo ago".blue())

        val evidenceBoolean = if (report.evidence != null) "Yes" else "No"

        item.addLoreLine("Evidence: &f$evidenceBoolean".blue())

        if ((report.status == ReportStatus.COMPLETED ||
                    report.status == ReportStatus.RE_OPENED)
            && viewer.hasPermission(Perm.REPORT_ADMIN)) {

            item.addLoreLine("Result: &f${report.action.displayName}".blue())
            item.addLoreLine("Staff member: &f${report.staffName}".blue())
            item.addLoreLine("Silent: &f${report.silent.toString().lowercase().capitalizeFirstLetter()}".blue())
        }

        item.addUsageLine(ClickType.LEFT, "open the report menu")
        item.addUsageLine(ClickType.RIGHT, "remove the report")

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if(clickType == ClickType.LEFT && clicker.hasPermission(Perm.REPORT_STAFF)) {
            ReportListMenu(clicker).open(clicker)
        } else if (clickType == ClickType.RIGHT && clicker.hasPermission(Perm.INVESTIGATION_ADMIN)) {
            ConfirmMenu(clicker) {
                IGService.removeReport(investigation.id, viewer.safeUuidStr(), report.id)
                clicker.sendSuccessMessage("Successfully removed report ${report.id} from the investigation.")
                Task.runLater ({
                    LinkedReportMenu(investigation.id, clicker).open(clicker)
                }, 500, TimeUnit.MINUTES)
            }.open(clicker)
        }
    }
}