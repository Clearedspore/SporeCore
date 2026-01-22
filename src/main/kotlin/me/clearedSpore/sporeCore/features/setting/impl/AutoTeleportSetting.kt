package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import org.bukkit.Material

@Setting
class AutoTeleportSetting : ToggleSetting(
    key = "auto-teleport",
    displayName = "Auto Teleport",
    item = Material.LIME_WOOL,
    lore = listOf(
        "| Controls whether to auto accept all /tpa requests."
    )
) {
    override fun defaultValue(): Boolean = false
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.teleportRequest
}