package me.clearedSpore.sporeCore.menu.investigation.manage.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeAPI.util.ItemBuilder
import me.clearedSpore.sporeCore.util.button.TextButton
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class IGInfoItem(
    val investigationID: String,
) : Item() {
    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        val item = ItemBuilder(Material.BOOK)
            .setName("Investigation Info".blue())

            .addLoreLine("")
            .addLoreLine("|".gray() + " Name: &f${investigation.name}".blue())
            .addLoreLine("|".gray() + " Creator: &f${investigation.getCreatorName() ?: "None".red()}".blue())
            .addLoreLine("|".gray() + " Description: &f${investigation.description}".blue())

        val age = System.currentTimeMillis() - investigation.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val date = formatter.format(
            Instant.ofEpochMilli(investigation.timestamp).atZone(ZoneId.systemDefault())
        )

        item.addLoreLine("|".gray() + " Timestamp: &f$date ($timeAgo)".blue())
        item.addLoreLine("|".gray() + " Notes: &f${investigation.notes.size}".blue())
        item.addLoreLine("|".gray() + " Linked Reports: &f${investigation.linkedReports.size}".blue())
        item.addLoreLine("|".gray() + " Linked Punishments: &f${investigation.linkedPunishments.size}".blue())
        item.addLoreLine("|".gray() + " Suspects/Players: &f${investigation.suspects.size}".blue())
        item.addLoreLine("|".gray() + " Staff: &f${investigation.staff.size}".blue())
        item.addLoreLine("|".gray() + " Priority: &f${investigation.getPriorityText()}".blue())
        item.addLoreLine("|".gray() + " Status: &f${investigation.status.displayName}".blue())
        item.addLoreLine("")

        item.addUsageLine(ClickType.LEFT, "copy the Investigation ID")
        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val investigation = IGService.findInvestigation(investigationID)!!
        if(clickType == ClickType.LEFT) {
            clicker.closeInventory()
            val button = TextButton("Click to copy!".blue())
                .copyToClipboard(investigation.id)
                .build(clicker)

            clicker.sendMessage(button)
        }
    }
}