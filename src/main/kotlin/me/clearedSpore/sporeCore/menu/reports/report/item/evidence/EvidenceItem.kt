package me.clearedSpore.sporeCore.menu.reports.report.item.evidence

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.reports.`object`.ReportType
import me.clearedSpore.sporeCore.menu.reports.report.EvidenceMenu
import me.clearedSpore.sporeAPI.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class EvidenceItem(
    val hasEvidence: Boolean,
    val target: OfflinePlayer,
    val reason: String,
    val type: ReportType,
    val instance: EvidenceMenu
) : Item() {
    override fun createItem(): ItemStack {
        val item = ItemBuilder(if (hasEvidence == true) Material.LIME_WOOL else Material.RED_WOOL)

        if (hasEvidence) {
            item.setName("Yes".green())
            item.addLoreLine("When the menu closes you will have to provide evidence".gray())
        } else {
            item.setName("No".red())
            item.addLoreLine("This will report the player without".gray())
            item.addLoreLine("providing evidence".gray())
        }

        return item.build()

    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (hasEvidence) {
            ChatInputService.begin(clicker) { input ->

                if(!input.startsWith("https://")){
                    clicker.sendErrorMessage("That is not a valid URL!")
                    clicker.sendErrorMessage("Your evidence must start with 'https://'. The procedure has been cancelled!")
                    return@begin
                }

                ReportService.report(clicker, target, reason, type, input)
            }
        } else {
            ReportService.report(clicker, target, reason, type)
            clicker.closeInventory()
        }
    }
}