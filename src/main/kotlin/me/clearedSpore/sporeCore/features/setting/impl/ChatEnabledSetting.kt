package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import org.bukkit.Material


@Setting
class ChatEnabledSetting : ToggleSetting(
    key = "chat-enabled",
    displayName = "Chat Enabled",
    item = Material.PAPER,
    lore = listOf(
        "| Controls whether you see chat messages",
        "| from players (excluding staff)"
    )
) {
    override fun defaultValue(): Boolean = true

    override fun isEnabledInConfig(config: CoreConfig): Boolean = true
}
