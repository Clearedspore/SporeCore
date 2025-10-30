package me.clearedSpore.sporeCore.menu.homes.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.homes.`object`.Home
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.util.TeleportService.awaitTeleport
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class HomeItem(
    private val player: Player,
    private val user: User,
    private val home: Home
) : Item() {

    private val homeService = SporeCore.instance.homeService

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.BLACK_BED)
        val meta = item.itemMeta
        meta?.setDisplayName(home.name.blue())
        meta?.lore = listOf(
            "Left click to teleport.".green(),
            "Shift + Right click to delete.".red()
        )
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        when {
            clickType.isLeftClick -> {
                clicker.closeInventory()
                player.awaitTeleport(home.location)
            }
            clickType.isRightClick && clickType.isShiftClick -> {
                ConfirmMenu(player) {
                    homeService.deleteHome(user, home.name)
                    player.sendSuccessMessage("Home ${home.name} has successfully been deleted!")

                }.open(player)
            }
        }
    }
}
