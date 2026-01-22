package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class TryLogSetting : ToggleSetting(
    key = "try-logs",
    displayName = "Try Logs",
    item = Material.BELL,
    lore = listOf(
        "| Controls whether you are able to see players",
        "| attempting to join while banned from the server."
    ),
    permission = Perm.PUNISH_LOG
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.punishments
}