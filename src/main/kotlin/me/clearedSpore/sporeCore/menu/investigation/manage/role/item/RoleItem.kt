package me.clearedSpore.sporeCore.menu.investigation.manage.role.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuidStr
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.IGService.logAction
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.IGLogType
import me.clearedSpore.sporeCore.menu.investigation.manage.role.RoleManageMenu
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class RoleItem(
    val investigation: Investigation,
    val viewer: Player,
    val staff: String,
    val type: RoleManageMenu.RoleType
) : Item() {

    override fun createItem(): ItemStack {
        val role = when (type) {
            RoleManageMenu.RoleType.ADMIN -> "Admin"
            RoleManageMenu.RoleType.STAFF -> "Staff"
        }
        val name = investigation.getName(staff)
        val item = ItemBuilder(Material.BOOK)
            .setName(name.blue())
            .addLoreLine("|".gray() + " UUID: &f$staff".blue())
            .addLoreLine("|".gray() + " Role: &f$role".blue())
            .addLoreLine("")

        if (viewer.hasPermission(Perm.INVESTIGATION_ADMIN) || investigation.creator == viewer.uuidStr()) {
            if (type == RoleManageMenu.RoleType.ADMIN) {
                item.addUsageLine(ClickType.LEFT, "demote to staff")
            } else {
                item.addUsageLine(ClickType.LEFT, "promote to admin")
            }
            item.addUsageLine(ClickType.RIGHT, "remove them from the investigation")
        }

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {

        if (clickType.isLeftClick && (viewer.hasPermission(Perm.INVESTIGATION_ADMIN) || investigation.creator == viewer.uuidStr())) {

            if (type == RoleManageMenu.RoleType.ADMIN) {
                investigation.admin.remove(staff)
                investigation.staff.add(staff)
            } else {
                investigation.admin.add(staff)
                investigation.staff.remove(staff)
            }

            val action = if (type == RoleManageMenu.RoleType.ADMIN) "demoted" else "promoted"

            IGService.updateInvestigation(investigation)
            clicker.sendSuccessMessage("Successfully $action ${investigation.getName(staff)}")
            logAction(investigation.id, IGLogType.STAFF, clicker.safeUuidStr(), "${action.capitalizeFirstLetter()} ${investigation.getName(staff)}")
            RoleManageMenu(investigation.id, clicker).open(clicker)
        }

        if (clickType == ClickType.RIGHT && (viewer.hasPermission(Perm.INVESTIGATION_ADMIN) || investigation.creator == viewer.uuidStr())
        ) {
            ConfirmMenu(clicker) {
                if (type == RoleManageMenu.RoleType.ADMIN) {
                    investigation.admin.remove(staff)
                } else {
                    investigation.staff.remove(staff)
                }
                IGService.updateInvestigation(investigation)
                clicker.sendSuccessMessage("Successfully removed ${investigation.getName(staff)}")
                logAction(investigation.id, IGLogType.STAFF, clicker.safeUuidStr(), "Removed ${investigation.getName(staff)} from the investigation")
                RoleManageMenu(investigation.id, clicker).open(clicker)
            }.open(clicker)
        }
    }
}