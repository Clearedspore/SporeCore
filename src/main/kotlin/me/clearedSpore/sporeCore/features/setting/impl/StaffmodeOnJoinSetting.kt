package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class StaffmodeOnJoinSetting : ToggleSetting(
    key = "staffmode-on-join",
    displayName = "Staff Mode On Join",
    item = Material.COMPASS,
    lore = listOf(
        "| Controls whether your staff mode is enabled",
        "| automatically when you join the server."
    ),
    permission = Perm.MODE_ALLOW
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.modes
}