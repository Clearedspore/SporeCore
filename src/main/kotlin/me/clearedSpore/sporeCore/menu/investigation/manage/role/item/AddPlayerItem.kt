package me.clearedSpore.sporeCore.menu.investigation.manage.role.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.IGService.logAction
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.IGLogType
import me.clearedSpore.sporeCore.menu.investigation.manage.role.RoleManageMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class AddPlayerItem(
    val investigation: Investigation,
    val viewer: Player,
) : Item() {

    override fun createItem(): ItemStack {
        return ItemBuilder(Material.LIME_WOOL)
            .setName("Add staff".blue())
            .addUsageLine(ClickType.LEFT, "add a new investigation staff member.")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ChatInputService.begin(clicker, false) { input ->
            val user = UserManager.get(input)

            if(user == null) {
                clicker.sendErrorMessage("That player does not exist!")
                return@begin
            }

            investigation.staff.add(user.uuidStr)
            IGService.updateInvestigation(investigation)
            clicker.sendSuccessMessage("Successfully added $input to the investigation.")
            logAction(investigation.id, IGLogType.STAFF, clicker.safeUuidStr(), "Added $input to the investigation.")
            RoleManageMenu(investigation.id, clicker).open(clicker)
        }
    }
}