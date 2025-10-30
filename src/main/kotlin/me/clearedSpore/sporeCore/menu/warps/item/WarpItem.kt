package me.clearedSpore.sporeCore.menu.warps.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.warp.`object`.Warp
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.TeleportService.awaitTeleport
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class WarpItem(
    private val warp: Warp,
    private val player: Player
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.OAK_SIGN)
        val meta = item.itemMeta

        meta?.setDisplayName(warp.name.blue())

        val lore = mutableListOf<String>()
        lore.add("Left click to teleport.".green())

        if (player.hasPermission(Perm.WARP_DELETE)) {
            lore.add("Shift + Right click to delete.".red())
        }

        meta?.lore = lore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        when {
            clickType.isLeftClick -> {
                clicker.closeInventory()
                clicker.awaitTeleport(warp.location)
            }
            clickType.isRightClick && clickType.isShiftClick -> {
                if (clicker.hasPermission(Perm.WARP_DELETE)) {
                    ConfirmMenu(clicker) {
                        SporeCore.instance.warpService.deleteWarp(warp.name)
                        clicker.sendSuccessMessage("Warp ${warp.name} has successfully been deleted!")
                    }.open(clicker)
                }
            }
        }
    }
}
