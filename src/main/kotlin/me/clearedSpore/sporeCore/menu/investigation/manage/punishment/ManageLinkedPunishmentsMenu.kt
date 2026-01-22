package me.clearedSpore.sporeCore.menu.investigation.manage.punishment

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.punishment.item.LinkedPunishmentItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import me.clearedSpore.sporeCore.menu.util.NoDataItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class ManageLinkedPunishmentsMenu(
    val investigationID: String,
    val viewer: Player
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Investigation | Linked Punishments"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val investigation = IGService.findInvestigation(investigationID) ?: return
        investigation.linkedPunishments.forEach { punishment ->
            addItem(LinkedPunishmentItem(investigation, punishment, viewer))
        }

        if(investigation.linkedPunishments.isEmpty()) {
            setGlobalMenuItem(5, 3, NoDataItem("No linked punishments", "There are currently no linked punishments."))
        }


        setGlobalMenuItem(5, 6, BackItem {
            ManageIGMenu(investigationID, viewer).open(viewer)
        })
    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {
    }
}