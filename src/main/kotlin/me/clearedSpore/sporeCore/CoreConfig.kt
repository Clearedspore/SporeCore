package me.clearedSpore.sporeCore

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration
import me.clearedSpore.sporeCore.features.eco.`object`.BalanceFormat
import org.intellij.lang.annotations.JdkConstants
import javax.swing.text.StyledEditorKit


@Configuration
data class CoreConfig(
    var general: GeneralConfig = GeneralConfig(),

    @Comment(
        "Toggle features you don't want/need!",
        "NOTE: Any feature you toggle requires a restart to apply!"
    )
    var features: FeaturesConfig = FeaturesConfig(),

    var economy: EconomyConfig = EconomyConfig()

)


@Configuration
data class GeneralConfig(

    @Comment(
        "This will be the prefix for ingame and console logging!"
    )
    var prefix: String = "SporeCore",

    @Comment(
    "When a player wants to teleport for example to spawn and",
    "they don't have the bypass permission (sporecore.bypass.teleport) they will",
    "need to wait a few seconds before being teleported. With this value you can set",
    "how long a player has to wait before being teleported."
    )
    var teleportTime: Int = 5
)

@Configuration
data class EconomyConfig(

    @Comment(
        "Enable or disable the economy system entirely.",
        "If false, Vault will not register this economy provider.",
        "When changing this option you will have to restart your server!"
    )
    var enabled: Boolean = true,

    @Comment(
        "The display name of this economy. This is how it appears in /vault-info, etc."
    )
    var name: String = "Spore Economy",

    @Comment(
        "Number of decimal digits to show in balances:",
        "0 → Whole numbers only (100)",
        "1 → One decimal (100.5)",
        "2 → Two decimals (100.50), etc."
    )
    var digits: Int = 2,

    @Comment(
        "Currency symbol (prefix before amount).",
        "Set to empty string if you don't want one."
    )
    var symbol: String = "$",

    @Comment(
        "Should there be a space between the symbol and the number?",
        "true → '$ 100.00', false → '$100.00'"
    )
    var spaceAfterSymbol: Boolean = false,

    @Comment(
        "Singular and plural currency names, used for display formatting.",
        "Example: 1 Coin / 5 Coins"
    )
    var singularName: String = "Coin",
    var pluralName: String = "Coins",

    @Comment(
        "If true, the symbol is placed before the number.",
        "If false, it’s placed after (e.g., '100 Coins')."
    )
    var symbolBeforeAmount: Boolean = true,

    @Comment(
        "Whether to use comma separators for thousands (1,000 vs 1000)."
    )
    var useThousandSeparator: Boolean = true,

    @Comment(
        "Default starting balance for new players."
    )
    var starterBalance: Double = 100.0,

    @Comment(
        "If true, every transaction is logged to console."
    )
    var logging: Boolean = true,

    @Comment(
        "If true, /baltop opens a GUI instead of a chat list."
    )
    var topMenu: Boolean = true,

    @Comment(
        "Allow players to pay each other via /pay."
    )
    var paying: Boolean = true,

    @Comment(
        "Choose how balances are displayed:",
        "PLAIN → 5000",
        "DECIMAL → 5000.00",
        "COMPACT → 5k, 5m, etc."
    )
    var balanceFormat: BalanceFormat = BalanceFormat.DECIMAL
)

@Configuration
data class FeaturesConfig(

    var teleportRequest: Boolean = true,

    @Comment("Examples: anvil, craftingtable, smithing, etc")
    var utilityMenus: Boolean = true,

    var privateMessages: Boolean = true,

    var spawn: Boolean = true,

    var settings: Boolean = true,

    var warps: Boolean = true,

    var homes: Boolean = true
)


