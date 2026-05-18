package me.clearedSpore.sporeCore.menu.reports.report.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.reports.`object`.ReportType
import me.clearedSpore.sporeCore.menu.reports.report.EvidenceMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class CustomReasonItem(
    val target: OfflinePlayer
) : Item() {
    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.PAPER).setName("Custom".blue())
            .addLoreLine("Click to report &f${target.name}".gray() + " with a custom reason.".gray()).build()
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ChatInputService.begin(clicker) { input ->
            if (SporeCore.instance.coreConfig.reports.evidence) {
                EvidenceMenu(target, input, ReportType.CUSTOM).open(clicker)
            } else {
                ReportService.report(clicker, target, input, ReportType.CUSTOM)
            }
        }
        clicker.closeInventory()
    }
}