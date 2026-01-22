package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class LogsSetting : ToggleSetting(
    key = "logs",
    displayName = "Logs",
    item = Material.HOPPER,
    lore = listOf("| Controls whether you see activity logs."),
    permission = Perm.LOG
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = true
}
