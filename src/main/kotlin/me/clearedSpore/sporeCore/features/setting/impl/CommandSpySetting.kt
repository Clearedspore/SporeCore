package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.ToggleSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

@Setting
class CommandSpySetting : ToggleSetting(
    key = "commandspy",
    displayName = "Command Spy",
    item = Material.CHAIN_COMMAND_BLOCK,
    lore = listOf(
        "",
        "| &fControls whether you receive notice",
        "| &fof players using commands"
    ),
    Perm.COMMAND_SPY
) {
    override fun defaultValue(): Boolean = false
    override fun isEnabledInConfig(config: CoreConfig): Boolean = config.features.commandSpy
}