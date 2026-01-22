package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class ChannelMessagesSetting : ToggleSetting(
    key = "channel-messages",
    displayName = "Staff Messages",
    item = Material.PAPER,
    lore = listOf(
        "| Controls whether you are able to see messages",
        "| sent by staff in staff channels."
    ),
    permission = Perm.CHANNEL_ALLOW
) {
    override fun defaultValue(): Boolean = true
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.channels
}