package me.clearedSpore.sporeCore.menu.investigation.manage.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.note.ManageNotesMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ManageNotesItem(
    val investigationID: String,
) : Item() {
    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        return ItemBuilder(Material.PAPER)
            .setName("Manage notes".blue())
            .addLoreLine("Notes: ${investigation.notes.size}".blue())
            .addUsageLine(ClickType.LEFT, "manage notes")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ManageNotesMenu(investigationID, clicker).open(clicker)
    }
}