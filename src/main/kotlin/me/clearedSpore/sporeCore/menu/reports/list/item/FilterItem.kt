package me.clearedSpore.sporeCore.menu.reports.list.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.features.reports.`object`.ReportStatus
import me.clearedSpore.sporeCore.menu.reports.list.ReportListMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class FilterItem(val current: ReportStatus) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.HOPPER)
            .setName("Filter".blue())
            .addLoreLine("Filter reports based on their status".gray())
            .addLoreLine("")
            .addLoreLine("Current filter: &f${current.displayName}".gray())
            .addLoreLine("Click to cycle to next filter".gray())
            .build()

        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val statuses = ReportStatus.values()
        var nextIndex = (current.ordinal + 1) % statuses.size
        var nextStatus = statuses[nextIndex]

        while(nextStatus == ReportStatus.RE_OPENED && !clicker.hasPermission(Perm.REPORT_ADMIN)) {
            nextIndex = (nextIndex + 1) % statuses.size
            nextStatus = statuses[nextIndex]
        }

        ReportListMenu(clicker, nextStatus).open(clicker)
    }

}
