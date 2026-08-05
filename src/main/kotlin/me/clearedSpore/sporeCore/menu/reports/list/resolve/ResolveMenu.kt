package me.clearedSpore.sporeCore.menu.reports.list.resolve

import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.features.reports.`object`.ReportAction
import me.clearedSpore.sporeCore.menu.reports.list.resolve.item.ResolveItem
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.entity.Player


class ResolveMenu(
    val viewer: Player,
    val report: Report
) : Menu(SporeCore.instance) {

    override fun getMenuName(): String {
        return "Resolve report"
    }

    override fun getRows(): Int {
        return 3
    }

    override fun setMenuItems() {

        setMenuItem(2, 2, ResolveItem(report, Material.LIME_WOOL, ReportAction.ACCEPTED, "Accept".green(), false))
        setMenuItem(7, 2, ResolveItem(report, Material.RED_WOOL, ReportAction.DENIED, "Deny".red(), false))

        if (viewer.hasPermission(Perm.REPORT_SILENT)) {
            setMenuItem(4, 2, ResolveItem(report, Material.LIME_CARPET, ReportAction.ACCEPTED, "Accept".green(), true))
            setMenuItem(5, 2, ResolveItem(report, Material.RED_CARPET, ReportAction.DENIED, "Deny".red(), true))
        }
    }
}