package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import org.bukkit.Material

@Setting
class PrivateMessagesSetting : ToggleSetting(
    key = "private-msgs",
    displayName = "Private Messages",
    item = Material.WRITTEN_BOOK,
    lore = listOf(
        "| Controls whether you receive private",
        "| messages from players (excluding staff)."
    )
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.privateMessages
}