package me.clearedSpore.sporeCore.menu.investigation.manage.note.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.note.Note
import me.clearedSpore.sporeCore.menu.investigation.manage.note.ManageNotesMenu
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeAPI.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Util.wrapWithColors
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NoteItem(
    val investigation: Investigation,
    val note: Note,
    val viewer: Player,
) : Item() {

    override fun createItem(): ItemStack {
        val description = wrapWithColors(note.text, 50)
        val age = System.currentTimeMillis() - note.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val date = formatter.format(
            Instant.ofEpochMilli(note.timestamp).atZone(ZoneId.systemDefault())
        )

        val item = ItemBuilder(Material.PAPER)
            .setName("Note: ${note.name}".blue())
            .addLoreLine("")
            .addLoreLine("Added: &f${note.addedBy}".blue())
            .addLoreLine("Timestamp: &f$date ($timeAgo)".blue())
            .addLoreLine("Text: &f$description".blue())
            .addLoreLine("")

        if (viewer.hasPermission(Perm.INVESTIGATION_ADMIN) || investigation.admin.contains(viewer.uuidStr())) {
            item.addUsageLine(ClickType.LEFT, "remove this note")
        }

        return item.build()

    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (clickType == ClickType.LEFT && (viewer.hasPermission(Perm.INVESTIGATION_ADMIN) || investigation.admin.contains(viewer.uuidStr()))) {
            ConfirmMenu(clicker) {
                IGService.removeNote(investigation.id, note.id, clicker.uuidStr())
                clicker.sendSuccessMessage("Successfully removed ${note.name}")
                ManageNotesMenu(investigation.id, clicker).open(clicker)
            }.open(clicker)
        }
    }
}