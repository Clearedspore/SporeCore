package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class PunishmentLogsSetting : ToggleSetting(
    key = "punishment-logs",
    displayName = "Punishment Logs",
    item = Material.DISPENSER,
    lore = listOf(
        "| Controls whether punishment logs are",
        "| shown to you in chat."
    ),
    permission = Perm.PUNISH_LOG
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.punishments
}