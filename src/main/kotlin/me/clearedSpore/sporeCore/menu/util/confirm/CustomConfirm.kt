package me.clearedSpore.sporeCore.menu.util.confirm

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.SporeCore
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class CustomConfirm(
    private val player: Player,
    private val confirmLore: List<String>,
    private val onConfirm: () -> Unit
) : Menu(SporeCore.instance) {

    override fun getMenuName(): String = "Are you sure?"
    override fun getRows(): Int = 3

    override fun setMenuItems() {
        setMenuItem(7, 2, object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.LIME_WOOL)
                val meta = item.itemMeta
                meta?.setDisplayName("Confirm".green())
                meta?.lore = confirmLore
                item.itemMeta = meta
                return item
            }

            override fun onClickEvent(clicker: Player, clickType: ClickType) {
                onConfirm()
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
                player.closeInventory()
            }
        })

        setMenuItem(3, 2, object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.RED_WOOL)
                val meta = item.itemMeta
                meta?.setDisplayName("Cancel".red())
                item.itemMeta = meta
                return item
            }

            override fun onClickEvent(clicker: Player, clickType: ClickType) {
                player.closeInventory()
            }
        })
    }

    override fun fillEmptySlots(): Boolean = true
}
