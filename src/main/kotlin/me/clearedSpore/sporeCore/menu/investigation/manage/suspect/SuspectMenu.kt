package me.clearedSpore.sporeCore.menu.investigation.manage.suspect

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.suspect.item.AddSuspectItem
import me.clearedSpore.sporeCore.menu.investigation.manage.suspect.item.SuspectItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import me.clearedSpore.sporeCore.menu.util.NoDataItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class SuspectMenu(
    val investigationID: String,
    val viewer: Player,
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Investigation | Suspects"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val investigation = IGService.findInvestigation(investigationID) ?: return
        investigation.suspects.forEach { suspect ->
            addItem(SuspectItem(investigation, suspect, viewer))
        }

        if(investigation.suspects.isEmpty()) {
            setGlobalMenuItem(5, 3, NoDataItem("No suspects", "There are currently no suspects."))
        }


        setGlobalMenuItem(4, 6, AddSuspectItem(investigationID, viewer))
        setGlobalMenuItem(6, 6, BackItem({ unit ->
            ManageIGMenu(investigationID, viewer).open(viewer)
        }))
    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}