package me.clearedSpore.sporeCore.menu.investigation.manage.note.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuidStr
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuid
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.IGService.logAction
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.IGLogType
import me.clearedSpore.sporeCore.features.investigation.`object`.note.Note
import me.clearedSpore.sporeCore.features.investigation.`object`.suspect.Suspect
import me.clearedSpore.sporeCore.menu.investigation.manage.note.ManageNotesMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.role.RoleManageMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.suspect.SuspectMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*

class AddNoteItem(
    val investigationID: String,
    val viewer: Player,
) : Item() {

    override fun createItem(): ItemStack {
        return ItemBuilder(Material.LIME_WOOL)
            .setName("Add note".blue())
            .addUsageLine(ClickType.LEFT, "add a new note to the investigation.")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val investigation = IGService.findInvestigation(investigationID)!!
        clicker.sendSuccessMessage("Please provide the note name")
        ChatInputService.begin(clicker, true) { name ->
            clicker.sendSuccessMessage("Please provide the note description")
            ChatInputService.begin(clicker, true) { description ->

                IGService.addNote(investigationID, name, description, clicker)
                IGService.updateInvestigation(investigation)
                clicker.sendSuccessMessage("Successfully added $name as a note to the investigation.")
                logAction(
                    investigation.id,
                    IGLogType.STAFF,
                    clicker.safeUuidStr(),
                    "Added $name as a note to the investigation."
                )
            }
        }
    }
}