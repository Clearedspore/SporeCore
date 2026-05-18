package me.clearedSpore.sporeCore.menu.settings.item

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.features.setting.SkullData
import me.clearedSpore.sporeCore.features.setting.model.AbstractSetting
import me.clearedSpore.sporeCore.features.setting.model.type.InputSetting
import me.clearedSpore.sporeCore.features.setting.model.type.OptionSetting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.user.User
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class SettingItem(
    private val user: User,
    private val setting: AbstractSetting<*>
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemStack(setting.item)
        val meta = item.itemMeta
        meta?.setDisplayName(setting.displayName.blue())

        val skullData: SkullData? = setting.getSkullData()
        if (skullData != null && item.type == Material.PLAYER_HEAD && meta is SkullMeta) {
            val profile: PlayerProfile = Bukkit.createProfile(UUID.randomUUID(), null)
            profile.setProperty(ProfileProperty("textures", skullData.texture, skullData.signature))
            meta.playerProfile = profile
        }


        val castedSetting = setting as AbstractSetting<Any>
        val storedValue = user.playerSettings[setting.key]
        val current = castedSetting.get(storedValue)

        val valueLore: List<String> = when (castedSetting) {
            is ToggleSetting -> listOf(
                "Value: ${if (castedSetting.get(storedValue)) "Enabled".green() else "Disabled".red()}".blue(),
                "",
                "Click to toggle".gray()
            )

            is InputSetting<*> -> listOf(
                "Value: ${castedSetting.get(storedValue).toString().green()}".blue(),
                "",
                "Click to set".gray()
            )

            is OptionSetting<*> -> {
                castedSetting.values().map { option ->
                    if (option == current) "• $option".green() else "• $option".red()
                } + listOf(
                    "",
                    "Left click to go forward".gray(),
                    "Right click to go backwards".gray()
                )
            }

            else -> listOf()
        }

        meta?.lore = setting.lore.map { it.gray() } + listOf("") + valueLore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val castedSetting = setting as AbstractSetting<Any>
        val storedValue = user.playerSettings[setting.key]
        val current = castedSetting.get(storedValue)

        val newValue: Any? = when (castedSetting) {
            is ToggleSetting -> castedSetting.onClick(clicker, current as Boolean)
            is InputSetting<*> -> {
                castedSetting.onClick(clicker, current)
                null
            }

            is OptionSetting<*> -> {
                val options = castedSetting.values()
                val index = options.indexOf(current)
                when (clickType) {
                    ClickType.LEFT -> options[(index + 1) % options.size]
                    ClickType.RIGHT -> options[(index - 1 + options.size) % options.size]
                    else -> current
                }
            }

            else -> null
        }

        if (newValue != null) {
            user.setSetting(castedSetting, newValue)
        }
    }
}
