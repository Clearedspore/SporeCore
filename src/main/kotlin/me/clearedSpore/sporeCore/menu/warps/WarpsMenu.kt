package me.clearedSpore.sporeCore.menu.warps

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.homes.HomeService
import me.clearedSpore.sporeCore.features.warp.WarpService
import me.clearedSpore.sporeCore.features.warp.`object`.Warp
import me.clearedSpore.sporeCore.menu.homes.item.HomeItem
import me.clearedSpore.sporeCore.menu.warps.item.NoWarpsItem
import me.clearedSpore.sporeCore.menu.warps.item.WarpItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class WarpsMenu(private val player: Player) : BasePaginatedMenu(SporeCore.instance, true) {

    private val warpService: WarpService = SporeCore.instance.warpService

    override fun getMenuName(): String = "Warps"
    override fun getRows(): Int = 6

    override fun createItems() {
        val warps = warpService.getAllWarps()

        if (warps.isEmpty()){
            setGlobalMenuItem(5, 3, NoWarpsItem())
            return
        }

        warps.forEach { warp ->
            if (warp.permission == null || player.hasPermission(warp.permission)) {
                addItem(WarpItem(warp, player))
            }
        }

    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}
