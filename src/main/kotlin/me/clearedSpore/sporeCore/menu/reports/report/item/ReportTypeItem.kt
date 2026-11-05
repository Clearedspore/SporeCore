package me.clearedSpore.sporeCore.menu.reports.report.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeCore.features.reports.`object`.ReportType
import me.clearedSpore.sporeCore.menu.reports.report.ReportMenu
import me.clearedSpore.sporeAPI.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class ReportTypeItem(
    val target: OfflinePlayer,
    val type: ReportType
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.BOOK)
            .setName(type.displayName.blue())
            .addLoreLine("Click to view report reasons for this type".gold())
            .build()

        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ReportMenu(clicker, target, type).open(clicker)
    }
}