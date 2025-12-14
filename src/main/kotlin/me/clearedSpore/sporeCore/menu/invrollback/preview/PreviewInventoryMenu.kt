package me.clearedSpore.sporeCore.menu.invrollback.preview

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.menu.invrollback.InvRollBackMenu
import me.clearedSpore.sporeCore.menu.invrollback.preview.item.ForceRollbackItem
import me.clearedSpore.sporeCore.menu.invrollback.preview.item.InfoItem
import me.clearedSpore.sporeCore.menu.invrollback.preview.item.LocationItem
import me.clearedSpore.sporeCore.menu.invrollback.preview.item.SoftRollbackItem
import me.clearedSpore.sporeCore.menu.kits.KitsMenu
import me.clearedSpore.sporeCore.menu.kits.preview.KitPreviewMenu.SimpleItem
import me.clearedSpore.sporeCore.menu.util.BackItem
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class PreviewInventoryMenu(
    private val data: InventoryData,
    private val player: Player,
    private val target: OfflinePlayer
) : Menu(SporeCore.instance) {

    override fun getMenuName(): String {
        return "Rollback | ${target.name}"
    }

    override fun getRows(): Int {
        return 6
    }

    override fun setMenuItems() {
        var index = 0

        for (y in 1..4) {
            for (x in 1..9) {
                val item = data.contents.getOrNull(index) ?: ItemStack(Material.AIR)
                setMenuItem(x, y, SimpleItem(item))
                index++
            }
        }



        for (x in 1..4) {
            val armorItem = data.armor.getOrNull(x - 1) ?: ItemStack(Material.AIR)
            setMenuItem(x, 5, SimpleItem(armorItem))
        }

        val offhandItem = data.offhand ?: ItemStack(Material.AIR)
        setMenuItem(6, 5, SimpleItem(offhandItem))

        setMenuItem(1, 6, BackItem { unit ->
            InvRollBackMenu(player, target).open(player)
        })

        setMenuItem(9, 6, SoftRollbackItem(target, data))
        setMenuItem(8, 6, ForceRollbackItem(target, data))
        setMenuItem(7, 6, InfoItem("Experience: ${data.experience.toString().green()}".blue(), Material.EXPERIENCE_BOTTLE, listOf()))
        setMenuItem(6, 6, LocationItem(player, data))

        fillEmptySlots()
    }

    private class SimpleItem(private val stack: ItemStack) : Item() {
        override fun createItem(): ItemStack = stack
        override fun onClickEvent(clicker: Player, clickType: ClickType) { }
    }
}