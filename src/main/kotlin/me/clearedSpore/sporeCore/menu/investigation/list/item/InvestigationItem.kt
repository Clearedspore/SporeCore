package me.clearedSpore.sporeCore.menu.investigation.list.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.time.TimeUtil
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
            .addLoreLine("|".gray() + " Name: &f${investigation.name}".blue())
            .addLoreLine("|".gray() + " Creator: &f${investigation.getCreatorName() ?: "None".red()}".blue())
            .addLoreLine("|".gray() + " Description: &f${investigation.description}".blue())
            .addLoreLine("|".gray() + " Timestamp: &f$date ($timeAgo)".blue())
            .addLoreLine("|".gray() + " Notes: &f${investigation.notes.size}".blue())
            .addLoreLine("|".gray() + " Linked Reports: &f${investigation.linkedReports.size}".blue())
            .addLoreLine("|".gray() + " Linked Punishments: &f${investigation.linkedPunishments.size}".blue())
            .addLoreLine("|".gray() + " Suspects/Players: &f${investigation.suspects.size}".blue())
            .addLoreLine("|".gray() + " Staff: &f${investigation.staff.size}".blue())
            .addLoreLine("|".gray() + " Admins: &f${investigation.admin.size}".blue())
            .addLoreLine("|".gray() + " Logs: &f${investigation.logs.size}".blue())
            .addLoreLine("|".gray() + " Priority: &f${investigation.getPriorityText()}".blue())
            .addLoreLine("|".gray() + " Status: &f${investigation.status.displayName}".blue())
            .addLoreLine("")
            .addUsageLine(ClickType.LEFT, "manage this investigation")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ManageIGMenu(investigationID, clicker).open(clicker)
    }
}