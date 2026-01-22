package me.clearedSpore.sporeCore.features.setting.impl

import me.clearedSpore.sporeCore.CoreConfig
import me.clearedSpore.sporeCore.annotations.Setting
import me.clearedSpore.sporeCore.features.setting.model.type.OptionSetting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

enum class MentionOption {
    TITLE_ENABLED,
    SOUND_ENABLED,
    TITLE_AND_SOUND,
    NONE;

    override fun toString(): String {
        return when (this) {
            TITLE_ENABLED -> "Mention title enabled"
            SOUND_ENABLED -> "Mention sound enabled"
            TITLE_AND_SOUND -> "Mention title & sound enabled"
            NONE -> "No mention sound or title enabled"
        }
    }
}

@Setting
class MentionTitleSetting : OptionSetting<MentionOption>(
    key = "mention",
    displayName = "Mentions",
    item = Material.LECTERN,
    lore = listOf(
        "If you want to hear the mention sound or if you want",
        "to see a title when a player mentions you in chat."
    ),
) {
    override fun values(): List<MentionOption> = listOf(
        MentionOption.TITLE_ENABLED,
        MentionOption.SOUND_ENABLED,
        MentionOption.TITLE_AND_SOUND,
        MentionOption.NONE
    )

    override fun defaultValue(): MentionOption = MentionOption.TITLE_AND_SOUND

    override fun isEnabledInConfig(config: CoreConfig): Boolean {
        return config.features.settings
    }

    override fun serialize(value: MentionOption): Any = value.name
}
