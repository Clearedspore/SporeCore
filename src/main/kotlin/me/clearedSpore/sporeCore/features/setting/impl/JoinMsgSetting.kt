package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import org.bukkit.Material

@Setting
class JoinMsgSetting : ToggleSetting(
    key = "join-msg",
    displayName = "Join message",
    item = Material.REDSTONE_LAMP,
    lore = listOf(
        "When you join do you want",
        "to see the info message?"
    )
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.join.message.isNotEmpty()
}