package me.clearedSpore.sporeCore.menu.investigation.manage.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.punishment.ManageLinkedPunishmentsMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ManageLinkedPunishmentsItem(
    val investigationID: String,
) : Item() {

    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        if(!SporeCore.instance.coreConfig.features.punishments){
            return ItemBuilder(Material.BARRIER)
                .setName("This feature is disabled!".red())
                .addLoreLine("Punishments are disabled in the config!".red())
                .build()
        }

        return ItemBuilder(Material.RED_WOOL)
            .setName("Manage linked punishments".blue())
            .addLoreLine("Linked punishments: ${investigation.linkedPunishments.size}".blue())
            .addUsageLine(ClickType.LEFT, "manage linked punishments")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ManageLinkedPunishmentsMenu(investigationID, clicker).open(clicker)
    }
}