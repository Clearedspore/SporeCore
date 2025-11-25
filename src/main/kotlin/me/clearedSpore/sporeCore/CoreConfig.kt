package me.clearedSpore.sporeCore

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration
import it.unimi.dsi.fastutil.booleans.BooleanList
import me.clearedSpore.sporeCore.features.chat.channel.`object`.Channel
import me.clearedSpore.sporeCore.features.eco.`object`.BalanceFormat
import java.awt.Color


@Configuration
data class CoreConfig(

    var general: GeneralConfig = GeneralConfig(),

    @Comment(
        "Toggle features you don't want/need!",
        "NOTE: Any feature you toggle requires a restart to apply!",
        "Some features require ingame permissions to use them.",
        "These permissions are always registered ingame."
    )
    var features: FeaturesConfig = FeaturesConfig(),

    var broadcastConfig: BroadcastConfig = BroadcastConfig(),

    var economy: EconomyConfig = EconomyConfig(),

    var kits: KitsConfig = KitsConfig(),

    var join: JoinConfig = JoinConfig(),

    var chat: ChatConfig = ChatConfig()
)

@Configuration
data class JoinConfig(

    @Comment(
        "Should players be teleported to spawn when they join?"
    )
    var spawnOnJoin: Boolean = true,

    @Comment(
        "What title should the players see when they join?",
        "You can use %player% to return the players name",
        "Leave it empty if you don't want a title"
    )
    var title: String = "&c&lWelcome %player%!",

    @Comment(
        "What message should the players receive when they join?",
        "You can use %player% to return the players name",
        "Leave it empty if you don't want to send messages"
    )
    var message: List<String> = listOf(
        "&cWelcome &b%player%",
        "&fRun &c/help &f to view the help menu!"
    ),

    @Comment(
        "The sound that should play (e.g., ENTITY_PLAYER_LEVELUP)",
        "when a player joins!"
    )
    var joinSound: String = "ENTITY_PLAYER_LEVELUP",

    @Comment(
        "What message should it broadcast when a player",
        "joins for the first time?",
        "You can use %player% to return the players name",
        "You can use %join_count% to see how many players",
        "have joined in total! This will add 1 to it since there",
        "is a new player that joined",
        "Leave it empty if you don't want it to broadcast it"
    )
    var firstJoinMessage: List<String> = listOf(
        "&b%player% &fhas joined for the first time!! &7%join_count%",
        "&cGive them a warm welcome!"
    ),

    @Comment(
        "What gamemode should the player be put in when they join?",
        "Leave it empty if you don't want it to set a gamemode"
    )
    var gamemode: String = "SURVIVAL"

)

@Configuration
data class ChatConfig(

    var chatColor: ChatColorConfig = ChatColorConfig(),

    @Comment(
        "You can enable or disable custom chat formatting using the 'enabled' flag.",
        "The 'format' string supports placeholders for player info and prefixes/suffixes.",
        "Supported placeholders:",
        "  %rankprefix% → Vault rank prefix of the player",
        "  %ranksuffix% → Vault rank suffix of the player",
        "  %player_name% → Name of the player sending the message",
        "  %message% → The actual chat message the player typed",
        "",
        "If PlaceholderAPI is installed, you can also use any PAPI placeholders here,",
        "for example: %player_health%, %server_online%, etc.",
        "",
        "Example format:",
        "  '%rankprefix% %player_name%: %message%'",
        "  → Outputs: [Admin] Steve: Hello everyone!"
    )
    var formatting: ChatFormatterConfig = ChatFormatterConfig(),

    var channels: ChatChannelsConfig = ChatChannelsConfig()
)

@Configuration
data class ChatFormatterConfig(

    var enabled: Boolean = true,

    var format: String = "%rankprefix% %player_name% %ranksuffix%: %message%"
)

@Configuration
data class ChatChannelsConfig(

    @Comment(
        "Add new chat channels you can use for ranked players",
        "The message would be the message everyone receives when",
        "you send a message in the channel.",
        "IN ORDER TO TYPE IN ANY CHANNEL THE PLAYER",
        "MUST HAVE THE sporecore.channel.allow PERMISSION!!",
        "Placeholders:",
        "%player% -> Player that sends the message",
        "%message% -> The message from the player"
    )
    var channels: MutableMap<String, ChannelConfig> = mutableMapOf(
        "staff" to ChannelConfig("Staff", "staff", "sporecore.channel.staff", "&b&lStaff", "&b&lStaff &7-> &9%player%&f: %message%", listOf("staffchat", "sc"), "#"),
        "admin" to ChannelConfig("Admin", "admin", "sporecore.channel.admin", "&c&lAdmin", "&c&lAdmin &7-> &c%player%&f: %message%", listOf("adminchat", "ac"), "@")
    )
)

@Configuration
data class ChannelConfig(
    var name: String = "Staff",
    var id: String = "staff",
    var permission: String = "sporecore.channel.staff",
    var prefix: String = "&b&lStaff",
    var message: String = "&b&lStaff &7-> &9%player%&f: %message%",
    var commands: List<String> = listOf("staff", "sc"),
    var symbol: String = "#"
)


@Configuration
data class ChatColorConfig(

    var enabled: Boolean = true,

    @Comment(
        "Add new color codes players can equip in chat.",
        "Each color code has its own permission.",
        "For example, for 'red' it would be 'sporecore.chatcolor.red'.",
        "",
        "You can now also specify a MATERIAL for GUI menus,",
        "so players can visually select their color.",
        "Example material names: RED_WOOL, GREEN_CONCRETE, PINK_TERRACOTTA, etc.",
        "",
        "⚠️ Don't use special characters (-, spaces, etc.) in the color key — it breaks permissions!"
    )
    var colors: MutableMap<String, ColorConfig> = mutableMapOf(
        "red" to ColorConfig("Red", "&c", "RED_WOOL"),
        "green" to ColorConfig("Green", "&a", "LIME_WOOL"),
        "blue" to ColorConfig("Blue", "&9", "BLUE_WOOL"),
        "yellow" to ColorConfig("Yellow", "&e", "YELLOW_WOOL"),
        "aqua" to ColorConfig("Aqua", "&b", "LIGHT_BLUE_WOOL"),
        "purple" to ColorConfig("Purple", "&5", "PURPLE_WOOL"),
        "gold" to ColorConfig("Gold", "&6", "ORANGE_WOOL"),
        "pink" to ColorConfig("Pink", "&d", "PINK_WOOL"),
        "gray" to ColorConfig("Gray", "&7", "LIGHT_GRAY_WOOL"),
        "customblue" to ColorConfig("Custom Blue", "&#1D91FF", "CYAN_WOOL")
    ),

    @Comment(
        "Default color code from one of the codes above."
    )
    var defaultColor: String = "gray"
)

@Configuration
data class ColorConfig(
    var name: String = "Default",
    var color: String = "&7",
    var material: String = "LIGHT_GRAY_WOOL"
)

@Configuration
data class BroadcastConfig(
    @Comment(
        "Prefix for the /broadcast command messages.",
        "You can use colors in the prefix with:",
        "  MiniMessage tags: <red>, <blue>, <bold>, etc.",
        "  Legacy codes: &c, &e, &l, etc.",
        "  Hex/RGB colors: &#RRGGBB (example: &#9C68D9)",
        "Example: '&7[&cBroadcast&7] &f'"
    )
    var broadcastPrefix: String = "&7[&cBroadcast&7] &f",

    @Comment(
        "Predefined broadcasts are messages that can be sent quickly.",
        "Each message can have placeholders {0}, {1}, etc., which are replaced",
        "by the arguments you provide in the command. Use quotes to provide arguments.",
        "",
        "Usage Examples:",
        "  /broadcast event \"CrystalPVP\" \"5 minutes\"",
        "    -> Broadcasts: {prefix} Event CrystalPVP is starting in 5 minutes!",
        "",
        "  /broadcast restart \"10\"",
        "    -> Broadcasts: {prefix}  Server restarting in 10 minutes!",
        "",
        "You can also send custom messages without using a predefined key:",
        "  /broadcast Hello everyone!",
        "",
        "Tips:",
        "  - Always enclose multi-word arguments in double quotes (\")",
        "  - Placeholders in the message template are zero-indexed: {0}, {1}, {2}, etc.",
        "  - Add as many placeholders as needed, but make sure to provide the same number of arguments."
    )
    var predefinedBroadcasts: MutableMap<String, String> = mutableMapOf(
        "event" to "&aEvent {0} is starting in {1}!",
        "restart" to "&cServer restarting in {0} minutes!"
    )
)

@Configuration
data class KitsConfig(

    @Comment(
        "Which kit should a player receive when they join for the first time and when.",
        "They die leave this empty if you don't want a kit selected.",
        "If you want to clear the players inventory before giving the kit",
        "Add |clear after it. Example:",
        "starter|clear"
    )
    var firstJoinKit: String = "starter",
    var deathKit: String = ""
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
    var teleportTime: Int = 5,
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
        "How many players it will show in the baltop menu.",
        "If you set the max amount too high it can cause server lag",
        "This is not recommended to change!"
    )
    var maxPlayers: Int = 250,

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
    var balanceFormat: BalanceFormat = BalanceFormat.COMPACT,
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

    var homes: Boolean = true,

    @Comment(
        "Allows players with the 'sporecore.chat.colored' permission to use",
        "color codes (e.g. &c, &b) and RGB colors in their messages.",
        "You only need to run /sporecore reload to apply changes — no server restart required."
    )
    var coloredChat: Boolean = true,

    var kits: Boolean = true,

    var stats: Boolean = true,

    var punishments: Boolean = true,

    var channels: Boolean = true,

    @Comment(
        "The currency feature is a separate currency that you",
        "can use as a 'premium' shop. You can modify the currency",
        "and add a shop in the (currency).yml file.",
        "If you change the file name and you don't have a file",
        "with that name it will automatically generate a new one."
    )
    var currency: CurrencyConfig = CurrencyConfig()
)

@Configuration
data class CurrencyConfig(

    var enabled: Boolean = true,

    var configFile: String = "credits.yml"
)


