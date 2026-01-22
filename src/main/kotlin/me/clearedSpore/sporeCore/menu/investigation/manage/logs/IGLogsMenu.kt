package me.clearedSpore.sporeCore.menu.investigation.manage.logs

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.IGLogType
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.logs.item.IGLogItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import me.clearedSpore.sporeCore.menu.util.EnumFilterItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class IGLogsMenu(
    val investigationID: String,
    val viewer: Player,
    val filter: IGLogType = IGLogType.ALL
) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String {
        return "Investigation | Logs"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun createItems() {
        val freshInvestigation = IGService.findInvestigation(investigationID) ?: return
        val logs = freshInvestigation.logs
            .sortedByDescending { it.timestamp }

        if (filter == IGLogType.ALL) {
            logs.forEach { log ->
                addItem(IGLogItem(freshInvestigation, log, viewer))
            }
        } else {
            logs.filter { it.type == filter }.forEach { log ->
                addItem(IGLogItem(freshInvestigation, log, viewer))
            }
        }

        val filterItem = EnumFilterItem(
            current = filter,
            values = IGLogType.values(),
            title = "Filter"
        ) { player, newStatus ->
            IGLogsMenu(investigationID, player, newStatus).open(player)
        }

        setGlobalMenuItem(4, 6, filterItem)
        addSearchItem(6, 6, listOf("Click to search by date".blue(), ""))
        setGlobalMenuItem(5, 6, BackItem {
            ManageIGMenu(investigationID, viewer).open(viewer)
        })
    }


    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}