package me.clearedSpore.sporeCore.menu.investigation.manage

import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.list.InvestigationListMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.item.CompleteIGItem
import me.clearedSpore.sporeCore.menu.investigation.manage.item.IGInfoItem
import me.clearedSpore.sporeCore.menu.investigation.manage.item.ManageLinkedPunishmentsItem
import me.clearedSpore.sporeCore.menu.investigation.manage.item.ManageLinkedReportsItem
import me.clearedSpore.sporeCore.menu.investigation.manage.item.ManageNotesItem
import me.clearedSpore.sporeCore.menu.investigation.manage.item.ManageStaffItem
import me.clearedSpore.sporeCore.menu.investigation.manage.item.ManageSuspectsItem
import me.clearedSpore.sporeCore.menu.investigation.manage.item.ViewIGLogsItem
import me.clearedSpore.sporeCore.menu.investigation.manage.logs.IGLogsMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.logs.item.IGLogItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import org.bukkit.entity.Player
import kotlin.io.path.fileVisitor

class ManageIGMenu(
    val investigationID: String,
    val viewer: Player
) : Menu(SporeCore.instance) {

    override fun fillEmptySlots(): Boolean {
        return true
    }

    override fun getMenuName(): String {
        return "Investigation | Manage"
    }

    override fun getRows(): Int {
        return 5
    }

    override fun setMenuItems() {

        val investigation = IGService.findInvestigation(investigationID) ?: return

        setMenuItem(5, 1, IGInfoItem(investigationID))

        setMenuItem(3, 2, ManageLinkedReportsItem(investigationID))
        setMenuItem(5, 2, ManageLinkedPunishmentsItem(investigationID))
        setMenuItem(7, 2, ManageSuspectsItem(investigationID))
        setMenuItem(4, 2, ManageNotesItem(investigationID))
        setMenuItem(6, 2, ManageStaffItem(investigationID))

        setMenuItem(4, 3, ViewIGLogsItem(investigationID))
        if(investigation.admin.contains(viewer.safeUuidStr())) {
            setMenuItem(6, 3, CompleteIGItem(investigationID))
        }
        setMenuItem(5, 4, BackItem({
            InvestigationListMenu(viewer).open(viewer)
        }))
    }
}