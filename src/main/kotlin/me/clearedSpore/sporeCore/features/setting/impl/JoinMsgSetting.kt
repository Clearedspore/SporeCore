package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import org.bukkit.Material

@Setting
class JoinMsgSetting : ToggleSetting(
    key = "join-msg",
    displayName = "Join Message",
    item = Material.REDSTONE_LAMP,
    lore = listOf(
        "| Controls whether you see",
        "| the info message on join"
    )
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.join.message.isNotEmpty()
}