package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class VanishOnMMLeaveSetting : ToggleSetting(
    key = "vanish-on-mm-leave",
    displayName = "Vanish On MM Leave",
    item = Material.ENDER_EYE,
    lore = listOf(
        "",
        "| &fControls whether you are still vanished",
        "| &feven after leaving any staff mode"
    ),
    permission = Perm.MODE_ALLOW
) {
    override fun defaultValue(): Boolean = false
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.modes
}