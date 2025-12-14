package me.clearedSpore.sporeCore.menu.invrollback.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.menu.invrollback.InvRollBackMenu
import me.clearedSpore.sporeCore.menu.invrollback.preview.PreviewInventoryMenu
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class InvItem(
    val viewer: Player,
    val data: InventoryData,
    val target: OfflinePlayer
) : Item() {

    override fun createItem(): ItemStack {
        val builder = ItemBuilder(Material.CHEST)
            .setName(data.id.gray())
            .addLoreLine("")
            .addLoreLine("Save reason:&f ${data.storeReason}".blue())
            .addLoreLine("XP:&f ${data.experience}".blue())
            .addLoreLine("Save location:&f ${data.formattedLocation()}".blue())
            .addLoreLine("Saved:&f ${data.formattedAge()}".blue())
            .addLoreLine("")
            .addLoreLine("Left click to restore".gold())

        if(viewer.hasPermission(Perm.INV_DELETE)){
            builder.addLoreLine("Right click to delete".gold())
        }

        return builder.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if(clickType.isLeftClick){
            PreviewInventoryMenu(data, clicker, target).open(clicker)
        } else if(clickType.isRightClick) {
            if(!clicker.hasPermission(Perm.INV_DELETE)) return

            ConfirmMenu(clicker) {
                InventoryManager.removeInventory(data.id)
                InvRollBackMenu(clicker, target).open(clicker)
                clicker.sendMessage("Successfully deleted an inventory.".blue())
            }.open(clicker)
        }
    }
}