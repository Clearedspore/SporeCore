package me.clearedSpore.sporeCore.menu.investigation.list.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class InvestigationItem(
    val investigationID: String,
    val index: Int,
) : Item() {

    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!

        val age = System.currentTimeMillis() - investigation.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val date = formatter.format(
            Instant.ofEpochMilli(investigation.timestamp).atZone(ZoneId.systemDefault())
        )

        return ItemBuilder(Material.BOOK)
            .setName("Investigation: &f$index".blue())
            .addLoreLine("")
            .addLoreLine("Name: &f${investigation.name}".blue())
            .addLoreLine("Creator: &f${investigation.getCreatorName() ?: "None".red()}".blue())
            .addLoreLine("Description: &f${investigation.description}".blue())
            .addLoreLine("Timestamp: &f$date ($timeAgo)".blue())
            .addLoreLine("Notes: &f${investigation.notes.size}".blue())
            .addLoreLine("Linked Reports: &f${investigation.linkedReports.size}".blue())
            .addLoreLine("Linked Punishments: &f${investigation.linkedPunishments.size}".blue())
            .addLoreLine("Suspects/Players: &f${investigation.suspects.size}".blue())
            .addLoreLine("Staff: &f${investigation.staff.size}".blue())
            .addLoreLine("Admins: &f${investigation.admin.size}".blue())
            .addLoreLine("Logs: &f${investigation.logs.size}".blue())
            .addLoreLine("Priority: &f${investigation.getPriorityText()}".blue())
            .addLoreLine("Status: &f${investigation.status.displayName}".blue())
            .addLoreLine("")
            .addUsageLine(ClickType.LEFT, "manage this investigation")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ManageIGMenu(investigationID, clicker).open(clicker)
    }
}