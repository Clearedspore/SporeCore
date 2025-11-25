package me.clearedSpore.sporeCore.menu.util.confirm

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.SporeCore
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.inventory.meta.ItemMeta

class TPAConfirmMenu(
    private val player: Player,
    private val target: Player,
    private val onConfirm: () -> Unit
) : Menu(SporeCore.instance) {

    override fun getMenuName(): String = "Are you sure?"
    override fun getRows(): Int = 3

    override fun setMenuItems() {
        setMenuItem(8, 2, object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.LIME_WOOL)
                val meta = item.itemMeta
                meta?.setDisplayName("Confirm".green())
                item.itemMeta = meta
                return item
            }

            override fun onClickEvent(clicker: Player, clickType: ClickType) {
                onConfirm()
                player.closeInventory()
            }
        })

        setMenuItem(2, 2, object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.RED_WOOL)
                val meta: ItemMeta? = item.itemMeta
                meta?.setDisplayName("Cancel".red())
                item.itemMeta = meta
                return item
            }

            override fun onClickEvent(clicker: Player, clickType: ClickType) {
                player.closeInventory()
            }
        })

        setMenuItem(5, 2, object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.PLAYER_HEAD)
                val meta = item.itemMeta as SkullMeta
                meta.owningPlayer = target
                meta.setDisplayName(target.name.blue())
                item.itemMeta = meta
                return item
            }
            override fun onClickEvent(clicker: Player, clickType: ClickType) {}
        })

        setMenuItem(4, 2, object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.ENDER_EYE)
                val meta = item.itemMeta

                val dimensionName = when (target.world.environment) {
                    World.Environment.NORMAL -> "Overworld"
                    World.Environment.NETHER -> "Nether"
                    World.Environment.THE_END -> "End"
                    World.Environment.CUSTOM -> "Custom"
                }

                meta?.setDisplayName("${target.name}'s Dimension".blue())
                meta?.lore = listOf(dimensionName.white())
                item.itemMeta = meta
                return item
            }

            override fun onClickEvent(clicker: Player, clickType: ClickType) {}
        })

        setMenuItem(6, 2, object : Item() {
            override fun createItem(): ItemStack {
                val item = ItemStack(Material.REDSTONE)
                val meta = item.itemMeta
                meta?.setDisplayName("${target.name}'s Ping".blue())
                meta?.lore = listOf("${Bukkit.getServer().getPlayer(target.uniqueId)?.ping} ms".white())
                item.itemMeta = meta
                return item
            }
            override fun onClickEvent(clicker: Player, clickType: ClickType) {}
        })
    }

    override fun fillEmptySlots(): Boolean = true
}
