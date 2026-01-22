package me.clearedSpore.sporeCore.menu.investigation.manage.report

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.list.InvestigationListMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.report.item.LinkedReportItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import me.clearedSpore.sporeCore.menu.util.NoDataItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class LinkedReportMenu(
    val investigationID: String,
    val viewer: Player
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Investigation | Linked Reports"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val investigation = IGService.findInvestigation(investigationID) ?: return
        investigation.linkedReports.forEach { linkedReport ->
            addItem(LinkedReportItem(investigation, linkedReport, viewer))
        }

        if(investigation.linkedReports.isEmpty()) {
            setGlobalMenuItem(5, 3, NoDataItem("No linked reports", "There are currently no linked reports."))
        }


        setGlobalMenuItem(5, 6, BackItem {
            ManageIGMenu(investigationID, viewer).open(viewer)
        })
    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}