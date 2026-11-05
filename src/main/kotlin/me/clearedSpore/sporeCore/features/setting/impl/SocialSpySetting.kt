package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class SocialSpySetting : ToggleSetting(
    key = "socialspy",
    displayName = "Social Spy",
    item = Material.OBSERVER,
    lore = listOf(
        "",
        "| &fControls whether you receive notice",
        "| &fof players messaging eachother."
    ),
    Perm.PM_BYPASS
) {
    override fun defaultValue(): Boolean = false
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.privateMessages
}