package me.clearedSpore.sporeCore.menu.investigation.manage.item

import lombok.`val`
import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.IdUtil
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationStatus
import me.clearedSpore.sporeCore.menu.investigation.manage.suspect.SuspectMenu
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.dizitart.no2.filters.FluentFilter.where

class CompleteIGItem(
    val investigationID: String,
) : Item() {

    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        if (investigation.status == InvestigationStatus.COMPLETED) {
            return ItemBuilder(Material.RED_WOOL)
                .setName("Delete Investigation".blue())
                .addUsageLine(ClickType.LEFT, "delete the investigation")
                .build()
        } else {
            return ItemBuilder(Material.LIME_WOOL)
                .setName("Complete Investigation".blue())
                .addUsageLine(ClickType.LEFT, "complete investigation")
                .build()
        }
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val investigation = IGService.findInvestigation(investigationID)!!
        if (investigation.status == InvestigationStatus.COMPLETED) {

            if(!clicker.hasPermission(Perm.INVESTIGATION_ADMIN)) {
                clicker.sendErrorMessage("Only players with the investigation admin permission can delete investigations!")
                return
            }

            ConfirmMenu(clicker) {
                val code = IdUtil.generateId(5)
                clicker.sendSuccessMessage("Please type the following code in chat $code")
                ChatInputService.begin(clicker, true) { input ->
                    if(code != input) {
                        clicker.sendErrorMessage("You did not type the correct code!")
                        return@begin
                    }

                    IGService.igCollection.remove(where("id").eq(investigation.id))
                    Logger.log(clicker, Perm.ADMIN_LOG, "deleted investigation ${investigation.name}", true)
                }
            }.open(clicker)
        } else {
            ConfirmMenu(clicker) {
                investigation.status = InvestigationStatus.COMPLETED
                IGService.updateInvestigation(investigation)
                Logger.log(clicker, Perm.ADMIN_LOG, "completed investigation ${investigation.name}", true)
            }.open(clicker)
        }
    }
}