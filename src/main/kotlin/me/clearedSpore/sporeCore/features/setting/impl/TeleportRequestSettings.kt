package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import org.bukkit.Material

@Setting
class TeleportRequestSettings : ToggleSetting(
    key = "teleport-requests",
    displayName = "Teleport Requests",
    item = Material.ENDER_EYE,
    lore = listOf(
        "| Controls whether players can send you teleport requests."
    )
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.teleportRequest
}