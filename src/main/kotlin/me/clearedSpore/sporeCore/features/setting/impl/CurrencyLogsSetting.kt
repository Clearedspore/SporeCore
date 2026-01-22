package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class CurrencyLogsSetting : ToggleSetting(
    key = "currency-logs",
    displayName = "Currency Logs",
    item = Material.GOLD_INGOT,
    lore = listOf(
        "| Controls whether changes in a player's currency",
        "| are sent to you."
    ),
    permission = Perm.CURRENCY_NOTIFY
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.currency.enabled
}