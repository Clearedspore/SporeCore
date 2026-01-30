package me.clearedSpore.sporeCore

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration
import me.clearedSpore.sporeCore.features.eco.`object`.BalanceFormat


@Configuration
data class CoreConfig(

    @Comment(
        "==========================================",
        "  ____                        ____         ",
        " / ___| _ __   ___  _ __ ___ / ___|___  _ __ ___ ",
        " \\___ \\| '_ \\ / _ \\| '__/ _ \\ |   / _ \\| '__/ _ \\",
        "  ___) | |_) | (_) | | |  __/ |__| (_) | | |  __/",
        " |____/| .__/ \\___/|_|  \\___|\\____\\___/|_|  \\___|",
        "       |_|                                      ",
        "==========================================",
        "SporeCore - Made by ClearedSpore",
        "",
        "SporeCore is a plugin that adds a lot of commands and features.",
        "Most of the big features and commands can be disabled.",
        "That is what makes SporeCore so great.",
        "If you have any questions you can join my discord.",
        "https://discord.gg/V2ZTgEr3M8",
        "or you can look at the wiki",
        "https://spore-plugins.gitbook.io/sporecore",
        "You can run /sporecore wiki <page> ingame to find",
        "other wiki pages!",
        "",
        "Some messages support PlaceholderAPI placeholders!",
        "You need placeholderAPI installed for this!",
        "",
        "You can use &cb for my own custom blue color!!"
    )
    var general: GeneralConfig = GeneralConfig(),

    @Comment(
        "Toggle features you don't want/need!",
        "NOTE: Any feature you toggle requires a restart to apply!",
        "Some features require ingame permissions to use them.",
        "These permissions are always registered ingame."
    )
    var features: FeaturesConfig = FeaturesConfig(),

    var discord: DiscordConfig = DiscordConfig(),

    var joinLeaveMessages: JoinLeaveMessages = JoinLeaveMessages(),

    var inventories: InventoryConfig = InventoryConfig(),

    var logs: LogsConfig = LogsConfig(),

    var broadcastConfig: BroadcastConfig = BroadcastConfig(),

    var economy: EconomyConfig = EconomyConfig(),

    var kits: KitsConfig = KitsConfig(),

    var join: JoinConfig = JoinConfig(),

    var chat: ChatConfig = ChatConfig(),

    var reports: ReportConfig = ReportConfig()
)

@Configuration
data class DiscordConfig(

    @Comment(
        "If you want to enable the discord features",
        "What does this do?",
        "- Add account linking",
        "- Channel message from and to discord",
        "- Discord punishments",
        "And much more!",
        "Not all features require the bot. For example the staff rollback",
        "ping only requires a webhook URL."
    )
    var enabled: Boolean = false,

    @Comment(
        "The token from your discord application",
        "You MUST provide this otherwise the bot features",
        "wont work."
    )
    var botToken: String = "",

    @Comment(
        "Rollback webhook URL",
        "For the ping you can set it to nothing or a role ID within <&>.",
        "Example: <@&1361382396603138098>",
        "In my private server that would be @Everyone"
    )
    var staffRollback: String = "",
    var staffRollbackPing: String = "@everyone",


    @Comment(
        "Punishment webhook URL",
        "These are webhook messages that will be sent",
        "when a staff member punishes someone.",
        "You can enable the staff ping. This would",
        "ping the punisher.",
        "THIS REQUIRES THE BOT FEATURE TO BE ENABLED!!"
    )
    var punishment: String = "",
    var pingStaff: Boolean = true,

    @Comment(
        "When a staff member rolls back someones inv",
        "it will send a message to the webhook.",
        "If you don't want a discord log for this",
        "then keep the webhook clear,"
    )
    var rollback: String = "",
    var rollbackPing: String = "",

    @Comment(
        "When a player sends a message in chat you can",
        "make it send in to a discord channel.",
        "If you don't want this feature leave it empty"
    )
    var chat: String = ""
)

@Configuration
data class ReportConfig(

    @Comment(
        "After how long should a report be deleted?"
    )
    var deletion: String = "2d",

    @Comment(
        "If a report has been handled how long should it take",
        "for the report to removed from the system."
    )
    var completedDeletion: String = "12h",

    @Comment(
        "What should the reporter see when their report has been finished?",
        "If you don't want a message leave it empty."
    )
    var notifyReporter: String = "&7[&6&lReports&7] &fYour recent report has been completed. Thank you for reporting that player and keeping our server safe!",

    @Comment(
        "The notification when a player has been reported 5 times in the last 10 minutes"
    )
    var tresHoldMessage: String = "&7[&6&lReports&7] &f%player% has been reported %count% times in the last 10 minutes!",

    @Comment(
        "The report accepted notification",
        "Placeholders:",
        "%staff% -> Staff member",
        "%target% -> player that got reported"
    )
    var reportAccepted: String = "&7[&6&lReports&7] &f%staff% has &aaccepted &fthe report against &7%target%",

    @Comment(
        "The report denied notification",
        "Placeholders:",
        "%staff% -> Staff member",
        "%target% -> player that got reported"
    )
    var reportDenied: String = "&7[&6&lReports&7] &f%staff% has &cdenied &fthe report against &7%target%",

    @Comment(
        "Notification for staff when a new report has been created",
        "Placeholders:",
        "%reporter% -> Reporter name",
        "%player% -> Player that got reported",
        "%reason% -> Reason for the report",
        "%type% -> Report type",
        "%evidence% -> If there is evidence (returns yes or no)",
        "",
        "You can use %button% if you want the following buttons there",
        "[Click to manage] -> Opens the report list menu",
        "[Click to view evidence] -> Views the evidence if provided"
    )
    var newReport: List<String> = listOf(
        "&6============= &lReport! &6=============",
        "&cbReporter: &f%reporter%",
        "&cbPlayer: &f%player%",
        "&cbReason: &f%reason% &7(%type%)",
        "&cbEvidence: &f%evidence%",
        "&cb%button%",
        "&6=================================="
    ),

    @Comment(
        "The color for the report buttons.",
        "Using &c or any other color is NOT supported!",
        "If you go ingame and your color is not applied then you",
        "haven't put in a valid color!!"
        )
    var buttonColor: String = "YELLOW",

    @Comment(
        "The report notification for when a report has been re-opened",
        "Placeholders:",
        "%staff% -> Staff member that re-openend the report",
        "%player% -> player that made the report"
    )
    var reportReOpened: String = "&7[&6&lReports&7] &f%staff% has re-opened %player%'s report",

    @Comment(
        "The message a player receives when their report has been re-opened",
        "Placeholders:",
        "%staff% -> Staff member that re-openend the report"
    )
    var reportReOpenedPlayer: String = "&7[&6&lReports&7] &fYour report has been re-opened by a staff member.",


    @Comment(
        "NOTE: Time in seconds!!"
    )
    var reportCooldown: Long = 60,

    @Comment(
        "Cooldown for when a player tries to report the same player",
    )
    var sameTargetCooldown: String = "1d",

    @Comment(
        "If you want players to be able to provide evidence"
    )
    var evidence: Boolean = true,

    @Comment(
        "Set report reasons players can choose",
        "You start with the reason and then you provide the",
        "report type after the |.",
        "For example the toxic reason would be chat",
        "You would do:",
        "Toxic|chat",
        "You can choose one of the following types:",
        "- cheating",
        "- chat",
        "- other"
    )
    var reportReasons: List<String> = listOf(
        "X-Ray|cheating",
        "Base-ESP|cheating",
        "Discrimination|chat",
        "Spamming|chat",
        "Inappropriate build|other"
    ),

    @Comment(
        "If players are allowed to use custom reasons"
    )
    var allowCustom: Boolean = true,

    @Comment(
        "When a staff member accepts the report should it open the punish menu?"
    )
    var openPunish: Boolean = true
)

@Configuration
data class InventoryConfig(

    @Comment(
        "How long will an inventory be stored?"
    )
    var deletion: String = "7d",

    @Comment(
        "Should it delete the inventory data",
        "When a staff member restores someone",
        "their inventory?"
    )
    var deleteAfterRestore: Boolean = true,

    var storeReasons: StoreReasonsConfig = StoreReasonsConfig()

)

@Configuration
data class StoreReasonsConfig(
    var death: Boolean = true,
    var join: Boolean = true,
    var leave: Boolean = true
)

@Configuration
data class LogsConfig(

    @Comment(
        "Enable or disable specific log types.",
        "Set 'false' to stop storing logs of that type.",
        "Here is an explanation for each log",
        "",
        "Join/Leave -> When a player joins or leaves the server",
        "Private Message -> When a player sends or receives a private message",
        "Teleport -> When a player teleports",
        "Chat -> When a player sends a chat message",
        "Command -> When a player runs a command",
        "Freeze -> When a player freezes someone or they get frozen."
    )
    var joinLeave: Boolean = true,
    var privateMessages: Boolean = true,
    var teleports: Boolean = true,
    var chat: Boolean = true,
    var commands: Boolean = true,
    var freeze: Boolean = true,

    @Comment(
        "Logs older than .... will be deleted",
        "You can ues the s, m, h, d format!",
        "example: 10s, 7m, 7d"
    )
    var cleanupTime: String = "7d"
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
        "Leave it empty if you don't want to send messages",
        "You can user PlaceholderAPI for placeholders.",
        "for %player_name% & %localtime_time% you need to",
        "download the LocalTime and Player using placeholderAPI",
        "You can do that by doing the following commands:",
        "- /papi ecloud download Player",
        "- /papi ecloud download LocalTime",
        "- /papi reload"
    )
    var message: List<String> = listOf(
        "&cbWelcome back %player_name%!!",
        "&cbTime: &f%localtime_time%",
        "&cbRun /help for a guide!"
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

    var mentions: MentionsConfig = MentionsConfig(),

    var channels: ChatChannelsConfig = ChatChannelsConfig()
)

@Configuration
data class MentionsConfig(

    @Comment(
        "If you want the mentions feature to be enabled. This makes it so",
        "when you say a players name in chat they receive a titl, actionbar, sound.",
        "You can modify what the player sees when they get mentioned"
    )
    var enabled: Boolean = true,

    @Comment(
        "Set this to nothing if you don't want to",
        "display a title!",
        "You can use the %player% placeholder",
        "to display the player that mentioned you."
    )
    var title: String = "&b&lMention!",

    var subTitle: String = "&f%player% has mentioned you!",

    var actionBar: String = "&f%player% &7has mentioned you in chat!",

    @Comment(
        "The sound the player will",
        "hear when they get mentioned!"
    )
    var sound: String = "ENTITY_PLAYER_LEVELUP"
)

@Configuration
data class ChatFormatterConfig(

    var enabled: Boolean = true,

    var format: String = "%rankprefix% %player_name% %ranksuffix%: %message%",

    @Comment(
        "When a player is in vanish and you use Luckperms",
        "for the vanish tag then it would display the tag",
        "in chat. (only if you have the ranksuffix in your formatter)",
        "This boolean makes it so that does not happen."
    )
    var hideVanishSuffix: Boolean = true
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
        "staff" to ChannelConfig(
            "Staff",
            "staff",
            "sporecore.channel.staff",
            "&b&lStaff",
            "&b&lStaff &7-> &9%player%&f: %message%",
            listOf("staffchat", "sc"),
            "#"
        ),
        "admin" to ChannelConfig(
            "Admin",
            "admin",
            "sporecore.channel.admin",
            "&c&lAdmin",
            "&c&lAdmin &7-> &c%player%&f: %message%",
            listOf("adminchat", "ac"),
            "@"
        )
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

    @Comment(
        "Text for the vanish placeholder",
        "%sporecore_vanish_tag%",
        "This will return with an empty line if the",
        "player is not vanished!"
    )
    var vanishTag: String = " &7[&bV&7]",

    @Comment(
        "Commands that can be run when a player",
        "is frozen. This can be bypassed",
        "with sporecore.bypass.freeze"
    )
    var freezeCommands: List<String> = listOf(
        "/msg",
        "/whisper"
    )
)

@Configuration
data class JoinLeaveMessages(

    @Comment(
        "Should there be join/leave messages?"
    )
    var enabled: Boolean = true,

    @Comment(
        "If someone goes in vanish should it send",
        "a leave message? This will also send a join",
        "message if the player disables vanish.",
        "This requires the vanish feature to be enabled"
    )
    var vanish: Boolean = true,

    @Comment(
        "join message",
        "leave it empty if you want no message"
    )
    var join: String = "&7[&a+&7] &f%player_name% &ahas joined the server",

    @Comment(
        "leave message",
        "leave it empty if you want no message"
    )
    var leave: String = "&7[&c-&7] &f%player_name% &chas left the server"
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

    var vanish: Boolean = true,

    var modes: Boolean = true,

    var invRollback: Boolean = true,

    var reports: Boolean = true,

    var investigation: Boolean = true,

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


