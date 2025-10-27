package me.clearedSpore.sporeCore.menu.homes

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.homes.HomeService
import me.clearedSpore.sporeCore.menu.homes.item.HomeItem
import me.clearedSpore.sporeCore.menu.homes.item.NoHomesItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class HomesMenu(private val player: Player) : BasePaginatedMenu(SporeCore.instance, true) {

    private val homeService: HomeService = SporeCore.instance.homeService

    override fun getMenuName(): String = "Your Homes"
    override fun getRows(): Int = 6

    override fun createItems() {
        val user = UserManager.get(player)

        if(user == null){
            player.userJoinFail()
            return
        }

        val homes = homeService.getAllHomes(user)
        if (homes.isEmpty()) {
            setGlobalMenuItem(5, 3, NoHomesItem())
            return
        }

        homes.forEach { home ->
            addItem(HomeItem(player, user, home))
        }
    }


    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}
