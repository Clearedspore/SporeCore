package me.clearedSpore.sporeCore.menu.investigation.manage.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.logs.IGLogsMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.report.LinkedReportMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ViewIGLogsItem(
    val investigationID: String,
) : Item() {

    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        return ItemBuilder(Material.HOPPER)
            .setName("View Logs".blue())
            .addLoreLine("Logs: ${investigation.logs.size}".blue())
            .addUsageLine(ClickType.LEFT, "manage logs")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        IGLogsMenu(investigationID, clicker).open(clicker)
    }
}