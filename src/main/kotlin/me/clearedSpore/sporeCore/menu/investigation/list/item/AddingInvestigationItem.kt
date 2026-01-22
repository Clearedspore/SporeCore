package me.clearedSpore.sporeCore.menu.investigation.list.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class AddingInvestigationItem(
    val investigation: Investigation,
    val index: Int,
    val id: String,
    val isReport: Boolean,
) : Item() {
    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.BOOK)
            .setName("Investigation: &f$index".blue())

            .addLoreLine("")
            .addLoreLine("Name: &f${investigation.name}".blue())
            .addLoreLine("Creator: &f${investigation.getCreatorName() ?: "None".red()}".blue())
            .addLoreLine("Description: &f${investigation.description}".blue())

        val age = System.currentTimeMillis() - investigation.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)
        val time = TimeUtil.formatDuration(investigation.timestamp, TimeUtil.TimeUnitStyle.LONG, 2)

        item.addLoreLine("Timestamp: &f$time ($timeAgo)".blue())
        item.addLoreLine("Notes: &f${investigation.notes.size}".blue())
        item.addLoreLine("Linked Reports: &f${investigation.linkedReports.size}".blue())
        item.addLoreLine("Linked Punishments: &f${investigation.linkedPunishments.size}".blue())
        item.addLoreLine("Suspects/Players: &f${investigation.suspects.size}".blue())
        item.addLoreLine("Staff: &f${investigation.staff.size}".blue())
        item.addLoreLine("Admins: &f${investigation.admin.size}".blue())
        item.addLoreLine("Logs: &f${investigation.logs.size}".blue())
        item.addLoreLine("Priority: &f${investigation.getPriorityText()}".blue())
        item.addLoreLine("Status: &f${investigation.status.displayName}".blue())
        item.addLoreLine("")
        val text = if (isReport) "Report" else "Punishment"
        item.addUsageLine(ClickType.LEFT, "add the $text to this investigation.")
        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if(isReport) {
            IGService.addReport(investigation.id, clicker.uuidStr(), id)
        } else {
            IGService.addPunishment(clicker, investigation.id, id)
        }
        ManageIGMenu(investigation.id, clicker).open(clicker)
    }
}