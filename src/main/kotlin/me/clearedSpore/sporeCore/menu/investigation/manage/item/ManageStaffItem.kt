package me.clearedSpore.sporeCore.menu.investigation.manage.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.role.RoleManageMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.suspect.SuspectMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ManageStaffItem(
    val investigationID: String,
) : Item() {

    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        return ItemBuilder(Material.BEACON)
            .setName("Manage Staff".blue())
            .addLoreLine("")
            .addLoreLine("|".gray() + " Staff: &f${investigation.staff.size}".blue())
            .addLoreLine("|".gray() + " Admins: &f${investigation.admin.size}".blue())
            .addLoreLine("")
            .addUsageLine(ClickType.LEFT, "manage staff")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        RoleManageMenu(investigationID, clicker).open(clicker)
    }
}