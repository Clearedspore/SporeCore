package me.clearedSpore.sporeCore.menu.investigation.list

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationStatus
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.menu.investigation.list.item.AddingInvestigationItem
import me.clearedSpore.sporeCore.menu.investigation.list.item.InvestigationItem
import me.clearedSpore.sporeCore.menu.investigation.list.item.NoInvestigationsItem
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class InvestigationListMenu(
    val viewer: Player,
    val id: String? = null,
    val isReport: Boolean? = false
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Investigations"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val investigations = IGService.igCollection
            .find()
            .mapNotNull { Investigation.fromDocument(it) }

        val sorted = investigations.sortedWith(
            compareByDescending<Investigation> { it.status == InvestigationStatus.IN_PROGRESS }
                .thenByDescending { it.timestamp }
        )

        if(id != null && isReport != null) {
            sorted.forEachIndexed { index, investigation ->
                if(investigation.staff.contains(viewer.uuidStr()) || viewer.hasPermission(Perm.INVESTIGATION_ADMIN)) {
                    addItem(AddingInvestigationItem(investigation, index, id, isReport))
                }
            }
            return
        }

        sorted.forEachIndexed { index, investigation ->
            if(investigation.staff.contains(viewer.uuidStr()) || viewer.hasPermission(Perm.INVESTIGATION_ADMIN)) {
                addItem(InvestigationItem(investigation.id, index + 1))
            }
        }

        if (sorted.isEmpty()) {
            setGlobalMenuItem(5, 3, NoInvestigationsItem())
        }
    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}