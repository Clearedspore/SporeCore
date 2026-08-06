package me.clearedSpore.sporeCore.menu.reports.list.resolve.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.features.reports.`object`.ReportAction
import me.clearedSpore.sporeCore.features.reports.`object`.ReportStatus
import me.clearedSpore.sporeAPI.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.dizitart.no2.filters.FluentFilter.where


class ResolveItem(
    val report: Report,
    val material: Material,
    val action: ReportAction,
    val actionStr: String,
    val silent: Boolean
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(material)

        if (!silent) {
            item.setName(actionStr)
            item.setLore("Click to ${actionStr.lowercase()}".gold() + " this report".gold())
        } else {
            item.setName(actionStr + " (Silent)".red())
            item.addLoreLine("Click to silently ${actionStr.lowercase()}".gold() + " this report.".gold())
            item.addLoreLine("That means the reporter will not be notified.".gray())
            item.addLoreLine("Staff will also not be notified.".gray())
        }

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val newReportDoc = ReportService.reportCollection
            .find(where("id").eq(report.id))
            .firstOrNull()

        val newReport = newReportDoc?.let { Report.fromDocument(it) }

        if (newReport == null) {
            clicker.sendErrorMessage("This report no longer exists!")
            clicker.closeInventory()
            return
        }

        if (newReport.status == ReportStatus.COMPLETED) {
            clicker.sendErrorMessage("That report has already been finished!")
            clicker.closeInventory()
            return
        }

        clicker.closeInventory()
        ReportService.completeReport(newReport.id, clicker, action, silent)
    }

}