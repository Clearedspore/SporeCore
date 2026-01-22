package me.clearedSpore.sporeCore.menu.investigation.manage.note

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.note.item.AddNoteItem
import me.clearedSpore.sporeCore.menu.investigation.manage.note.item.NoteItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import me.clearedSpore.sporeCore.menu.util.NoDataItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class ManageNotesMenu(
    val investigationID: String,
    val viewer: Player
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Investigation | Manage Notes"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {

        val investigation = IGService.findInvestigation(investigationID) ?: return

        investigation.notes.forEach { note ->
            addItem(NoteItem(investigation, note, viewer))
        }

        if(investigation.notes.isEmpty()) {
            setGlobalMenuItem(5, 3, NoDataItem("No notes", "There are currently no notes."))
        }


        setGlobalMenuItem(4, 6, AddNoteItem(investigationID, viewer))

        setGlobalMenuItem(6, 6, BackItem {
            ManageIGMenu(investigationID, viewer).open(viewer)
        })

    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}