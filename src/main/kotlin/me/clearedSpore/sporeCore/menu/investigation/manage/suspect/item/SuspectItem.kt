package me.clearedSpore.sporeCore.menu.investigation.manage.suspect.item

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.suspect.Suspect
import me.clearedSpore.sporeCore.menu.investigation.manage.suspect.SuspectMenu
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Util.wrapWithColors
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SuspectItem(
    val investigation: Investigation,
    val suspect: Suspect,
    val viewer: Player
) : Item() {

    override fun createItem(): ItemStack {

        val age = System.currentTimeMillis() - suspect.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)
        val time = TimeUtil.formatDuration(suspect.timestamp, TimeUtil.TimeUnitStyle.LONG, 2)

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val date = formatter.format(
            Instant.ofEpochMilli(suspect.timestamp).atZone(ZoneId.systemDefault())
        )

        val item = ItemBuilder(Material.PLAYER_HEAD)
            .setName((suspect.getSuspectName() ?: "None".red()).blue())
            .addLoreLine("")
            .addLoreLine("Description: &f${suspect.description}".blue())
            .addLoreLine("Added by: &f${investigation.getName(suspect.addedBy)}".blue())
            .addLoreLine("Timestamp: &f$date ($timeAgo)".blue())
            .addLoreLine("")

        if (viewer.hasPermission(Perm.HISTORY_OTHERS)) {
            item.addUsageLine(ClickType.LEFT, "open their history")
        }

        if (viewer.hasPermission(Perm.INVESTIGATION_ADMIN) || investigation.admin.contains(viewer.uuidStr())) {
            item.addUsageLine(ClickType.RIGHT, "delete the suspect")
        }

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (clickType == ClickType.LEFT && clicker.hasPermission(Perm.HISTORY_OTHERS)) {
            clicker.closeInventory()
            clicker.performCommand("history ${suspect.getSuspectName()}")
        } else if (clickType == ClickType.RIGHT && (clicker.hasPermission(Perm.INVESTIGATION_ADMIN) || investigation.admin.contains(viewer.uuidStr()))) {
            ConfirmMenu(clicker) {
                IGService.removeSuspect(investigation.id, viewer.uuidStr(), suspect.id)
                clicker.sendSuccessMessage("Successfully removed ${suspect.getSuspectName()}")
                SuspectMenu(investigation.id, clicker).open(clicker)
            }.open(clicker)
        }
    }
}