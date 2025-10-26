package me.clearedSpore.sporeCore.menu.settings

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.menu.settings.item.SettingItem
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.user.settings.Setting
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class SettingsMenu(private val player: Player) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String = "Player Settings"
    override fun getRows(): Int = 6

    override fun createItems() {
        val user = UserManager.get(player)

        if(user == null){
            player.userFail()
            return
        }

        val features = SporeCore.instance.coreConfig.features

        Setting.entries.forEach { setting ->
            if (setting.permission != null && !player.hasPermission(setting.permission)) return@forEach

            val key = setting.configKey
            if (key != null) {
                val enabled = when (key) {
                    "teleportRequest" -> features.teleportRequest
                    "privateMessages" -> features.privateMessages
                    else -> true
                }
                if (!enabled) return@forEach
            }

            addItem(SettingItem(user, setting))
        }
    }


    override fun onInventoryClickEvent(clicker: Player, clickType: ClickType, event: InventoryClickEvent) {}
}
