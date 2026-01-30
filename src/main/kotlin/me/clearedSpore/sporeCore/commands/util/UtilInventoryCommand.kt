package me.clearedSpore.sporeCore.commands.util

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@SporeCoreCommand
class UtilInventoryCommand : BaseCommand() {

    @Subcommand("inventory view")
    @CommandPermission(Perm.UTIL_INVENTORY)
    @CommandCompletion("@players")
    fun view(sender: Player, @Name("target") targetObject: OnlinePlayer) {
        val target = targetObject.player
        InventoryPreviewMenu(target, sender).open(sender)
    }

    @Subcommand("inventory dropall")
    @CommandPermission(Perm.UTIL_INVENTORY)
    @CommandCompletion("@players")
    fun dropAll(sender: Player, @Name("target") targetObject: OnlinePlayer) {
        val target = targetObject.player

        target.inventory.contents.forEach { item ->
            if (item != null && item.type != Material.AIR) target.world.dropItemNaturally(target.location, item)
        }

        InventoryManager.clearPlayerInventory(target)
        sender.sendSuccessMessage("Dropped all items from ${target.name}'s inventory.")
    }

    @Subcommand("inventory giveall")
    @CommandPermission(Perm.UTIL_INVENTORY)
    fun giveAll(sender: Player) {
        val items = sender.inventory.contents.filterNotNull()

        for (player in Bukkit.getOnlinePlayers()) {
            player.inventory.addItem(*items.toTypedArray())
        }

        sender.sendSuccessMessage("Gave your inventory to all online players.")
    }

    @Subcommand("inventory give")
    @CommandPermission(Perm.UTIL_INVENTORY)
    @CommandCompletion("@players")
    fun give(sender: Player, @Name("target") targetObject: OnlinePlayer) {
        val target = targetObject.player
        val items = sender.inventory.contents.filterNotNull()
        target.inventory.addItem(*items.toTypedArray())

        sender.sendSuccessMessage("Gave your inventory to ${target.name}.")
    }

    @Subcommand("inventory clearall")
    @CommandCompletion("@materials")
    @CommandPermission(Perm.UTIL_INVENTORY)
    fun clearAll(sender: Player, @Name("material") material: Material) {
        val item = ItemStack(material)

        for (player in Bukkit.getOnlinePlayers()) {
            player.inventory.remove(item)
        }

        sender.sendSuccessMessage("Cleared all ${material.name.lowercase()} from all inventories.")
    }

    private class InventoryPreviewMenu(
        private val target: Player,
        private val viewer: Player
    ) : Menu(SporeCore.instance) {

        override fun fillEmptySlots() = true
        override fun getMenuName(): String = "${target.name}'s Inventory"
        override fun getRows(): Int = 6

        override fun setMenuItems() {
            var index = 0
            for (y in 1..4) {
                for (x in 1..9) {
                    val item = target.inventory.contents.getOrNull(index) ?: ItemStack(Material.AIR)
                    setMenuItem(x, y, SimpleItem(item))
                    index++
                }
            }

            for (i in target.inventory.armorContents.indices) {
                val item = target.inventory.armorContents[i] ?: ItemStack(Material.AIR)
                setMenuItem(i + 1, 5, SimpleItem(item))
            }

            val offhand = target.inventory.itemInOffHand ?: ItemStack(Material.AIR)
            setMenuItem(6, 5, SimpleItem(offhand))

            val expItem = ItemStack(Material.EXPERIENCE_BOTTLE)
            val expMeta = expItem.itemMeta
            expMeta?.setDisplayName("Experience: ${target.exp} XP, Level ${target.level}")
            expItem.itemMeta = expMeta
            setMenuItem(7, 5, object : Item() {
                override fun createItem(): ItemStack = expItem
                override fun onClickEvent(clicker: Player, clickType: ClickType) {
                    when (clickType) {
                        ClickType.LEFT -> target.giveExp(1)
                        ClickType.RIGHT -> target.giveExp(-1)
                        ClickType.SHIFT_LEFT -> target.level += 1
                        ClickType.SHIFT_RIGHT -> target.level = maxOf(0, target.level - 1)
                        else -> {}
                    }
                }
            })

            for (i in 0 until 4) {
                val craftingItem = target.openInventory.topInventory.contents.getOrNull(i) ?: ItemStack(Material.AIR)
                setMenuItem(i + 6, 1, SimpleItem(craftingItem))
            }

            val clearItem = ItemStack(Material.BARRIER)
            val clearMeta = clearItem.itemMeta
            clearMeta?.setDisplayName("Clear Inventory")
            clearItem.itemMeta = clearMeta
            setMenuItem(9, 6, object : Item() {
                override fun createItem(): ItemStack = clearItem
                override fun onClickEvent(clicker: Player, clickType: ClickType) {
                    target.inventory.clear()
                    target.updateInventory()
                    viewer.sendSuccessMessage("${target.name}'s inventory has been cleared.")
                }
            })
        }

        private class SimpleItem(private val stack: ItemStack) : Item() {
            override fun createItem(): ItemStack = stack
            override fun onClickEvent(clicker: Player, clickType: ClickType) {}
        }
    }
}
