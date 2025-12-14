package me.clearedSpore.sporeCore.menu.invrollback.preview.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gold
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class LocationItem(
    private val viewer: Player,
    private val data: InventoryData
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.COMPASS)
            .setName("Location".red())
            .addLoreLine("Location: &f${data.formattedLocation()}".blue())

        if (viewer.hasPermission(Perm.INV_TELEPORT)) {
            item.addLoreLine("")
            item.addLoreLine("Click to teleport".gold())
        }

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {

        if (!clicker.hasPermission(Perm.INV_TELEPORT)) return

        val location = data.saveLocation

        if (location != null) {
            clicker.closeInventory()
            clicker.teleport(location)
            clicker.sendSuccessMessage("Teleported to save location!")
        } else {
            clicker.sendErrorMessage("Failed to find location!")
        }
    }
}