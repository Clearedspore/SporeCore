package me.clearedSpore.sporeCore.menu.investigation.manage.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.report.LinkedReportMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ManageLinkedReportsItem(
    val investigationID: String,
) : Item() {

    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        if(!SporeCore.instance.coreConfig.features.reports){
            return ItemBuilder(Material.BARRIER)
                .setName("This feature is disabled!".red())
                .addLoreLine("Reports are disabled in the config!".red())
                .build()
        }

        return ItemBuilder(Material.DIAMOND_SWORD)
            .setName("Manage linked reports".blue())
            .addLoreLine("Linked reports: ${investigation.linkedReports.size}".blue())
            .addUsageLine(ClickType.LEFT, "manage linked reports")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        LinkedReportMenu(investigationID, clicker).open(clicker)
    }
}