package me.clearedSpore.sporeCore.menu.kits.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.kit.`object`.Kit
import me.clearedSpore.sporeCore.menu.kits.preview.KitPreviewMenu
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class KitItem(
    private val kit: Kit,
    private val player: Player
) : Item() {

    val kitService = SporeCore.instance.kitService

    override fun createItem(): ItemStack {
        val item = ItemStack(kit.displayItem ?: Material.CHEST)
        val meta = item.itemMeta

        meta?.setDisplayName(kit.name.blue())

        val user = UserManager.get(player)!!


        val canClaim = !user.hasKitCooldown(kit.id)

        val lore = mutableListOf<String>()
        if (canClaim) {
            lore.add("Left click to claim".blue())
        } else {
            val timeleft = TimeUtil.formatDuration(user.getKitCooldownRemaining(kit.id))
            lore.add("You are currently on cooldown!".red())
            lore.add("Time left: $timeleft".red())
        }


        lore.add("Right click to preview this kit!".blue())

        meta?.lore = lore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        when {
            clickType.isLeftClick -> {
                clicker.closeInventory()
                kitService.giveKit(player, kit.name)
            }

            clickType.isRightClick -> {
//                NewKitPreviewMenu(
//                    clicker,
//                    kit.inventory.toTypedArray(),
//                    kit.armor.toTypedArray(),
//                    kit.offHand,
//                    "Previewing kit: ${kit.name.blue()}"
//                ).open()
                KitPreviewMenu(kit, player).open(player)
            }
        }
    }
}
