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
            TITLE_ENABLED -> "Title"
            SOUND_ENABLED -> "Sound"
            TITLE_AND_SOUND -> "Title & Sound"
            NONE -> "Disabled"
        }
    }
}

@Setting
class MentionTitleSetting : OptionSetting<MentionOption>(
    key = "mention",
    displayName = "Mentions",
    item = Material.LECTERN,
    lore = listOf(
        "| Controls what you want to see or",
        "| hear when you are mentioned in chat."
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
