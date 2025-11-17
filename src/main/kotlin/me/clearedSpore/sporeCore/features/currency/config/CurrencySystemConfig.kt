package me.clearedSpore.sporeCore.features.currency.config

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration

@Configuration
data class CurrencySystemConfig(

    @Comment(
        "Settings for how your currency looks, formats, and behaves.",
    )
    var currencySettings: CurrencySettingConfig = CurrencySettingConfig(),

    @Comment(
        "Configuration for the in-game currency shop, including categories and items.",
        "You can use the following placeholders in all the items:",
        "%player% -> Returns with the viewers name",
        "%balance% -> Returns with the viewers balance",
        "%currency% -> Returns with the plural currency name",
        "%singular_currency% -> Returns with the singular currency name",
        "%symbol% -> Returns with the currency symbol",
        "%package_name% -> Returns with the package someone bought.",
        "   Only works for the bought broadcast",
        "%costs% -> Returns with how much a package costs.",
        "    Only works for the bought broadcast",
        "And all PlaceholderAPI placeholders",
        "",
        "For all the menu items you don't actually provide the slot number",
        "but the X and Y coordinates. What do I mean by that?",
        "",
        "This means instead of calculating the inventory slot as a single number (0-53),",
        "you simply specify the row and column where you want the item to appear.",
        "For example, '5|2' means row 5, column 2 in the menu grid."
    )
    var shop: CurrencyShopConfig = CurrencyShopConfig(
        menuSettings = MenuSettingsConfig(
            main = MenuRowConfig(rows = 4, fillItems = true),
            categories = mutableMapOf(
                "ranks" to MenuRowConfig(rows = 5, fillItems = true)
            )
        ),
        categories = mutableMapOf(
            "ranks" to ShopCategoryConfig(
                name = "Ranks",
                slot = "3|5",
                displayItem = DisplayItemConfig(
                    material = "PAPER",
                    name = "&eRanks",
                    description = mutableListOf("&7Purchase your permanent ranks here!")
                ),
                items = mutableMapOf(
                    0 to ShopItemConfig(
                        slot = "2|2",
                        name = "Bronze Rank",
                        description = mutableListOf(
                            "&fReceive the &6[Bronze] &ftag!",
                            "&fPerks:",
                            "&e/bronze"
                        ),
                        price = 100.0,
                        material = "PAPER",
                        higherRanks = mutableListOf(),
                        purchaseCommands = mutableListOf("/lp user %player% parent add bronze")
                    ),
                    1 to ShopItemConfig(
                        slot = "2|3",
                        name = "Silver Rank",
                        description = mutableListOf(
                            "&fReceive the &7[Silver] &ftag!",
                            "&fPerks:",
                            "&e/silver"
                        ),
                        price = 250.0,
                        material = "IRON_INGOT",
                        higherRanks = mutableListOf("group.gold"),
                        purchaseCommands = mutableListOf("/lp user %player% parent add silver")
                    )
                )
            )
        ),
        infoItems = mutableMapOf(
            1 to GlobalInfoItemConfig(
                material = "BOOK",
                menu = "main",
                slot = "2|5",
                name = "&bInformation",
                description = mutableListOf(
                    "&7Earn %currency% &7through &avoting &7or buying at &e/store!",
                    "&7Spend them here for awesome &e&lrewards!"
                )
            )
        )
    )
)

@Configuration
data class CurrencySettingConfig(
    @Comment("Display names for your currency.")
    var singularName: String = "Credit",
    var pluralName: String = "Credits",

    @Comment(
        "Color code for the currency symbol/name (e.g. §5 for dark purple)",
        "You can use:",
        "MiniMessages: <red>",
        "RRGGBB: &#9C68D9",
        "Normal codes: &f, &c, &e, etc",
    )
    var currencyColor: String = "&#9C68D9&l",

    @Comment("Command alias for the shop command.")
    var shopCommand: List<String> = listOf("creditshop"),

    @Comment("Currency symbol configuration (e.g. ✦100 vs 100✦).")
    var symbol: String = "✦",
    var symbolBeforeAmount: Boolean = true,
    var spaceAfterSymbol: Boolean = true,

    @Comment("If it should display the currency name after the amount")
    var nameAfterAmount: Boolean = true,

    @Comment("1,000 vs 1000 formatting.")
    var useThousandSeparator: Boolean = true,

    @Comment("0 → 100 | 2 → 100.50 | etc.")
    var decimalDigits: Int = 0,

    @Comment("Display format: PLAIN (1000), DECIMAL (1000.00), COMPACT (1k, 1m, etc).")
    var balanceFormat: String = "PLAIN",

    @Comment("Starting balance for new players.")
    var startingBalance: Double = 0.0,

    @Comment("Name of the in-game shop menu.")
    var shopName: String = "Credit Shop",

    @Comment("Enable or disable the in-game shop feature.")
    var shopEnabled: Boolean = true,

    @Comment(
        "If it should broadcast when a player receives or loses credits",
        "only players with the sporecore.currency.notify will see this message"
    )
    var broadcastStaff: Boolean = true,

    @Comment("Messages that will be sent when someone buys a package")
    var broadcastMessages: List<String> = listOf(
        "&c[CreditShop Alert!] &f%player% has bought the %package_name% &ffor &a%costs%"
    )
)

@Configuration
data class CurrencyShopConfig(
    @Comment("Menu row settings for main menu and categories.")
    var menuSettings: MenuSettingsConfig = MenuSettingsConfig(),

    @Comment(
        "When setting items (including info items) you cannot set them",
        "in the bottom row of the menu. Meaning you can't set items in",
        "slot 45 - 54",
        "All shop categories."
    )
    var categories: MutableMap<String, ShopCategoryConfig> = mutableMapOf(),

    @Comment("Global info items that appear in any menu.")
    var infoItems: MutableMap<Int, GlobalInfoItemConfig> = mutableMapOf()
)

@Configuration
data class MenuSettingsConfig(
    var main: MenuRowConfig = MenuRowConfig(),
    var categories: MutableMap<String, MenuRowConfig> = mutableMapOf()
)

@Configuration
data class MenuRowConfig(
    var rows: Int = 4,
    var fillItems: Boolean = true
)

@Configuration
data class ShopCategoryConfig(
    var name: String = "Ranks",
    var slot: String = "3|5",

    @Comment("The item representing this category in the main shop menu.")
    var displayItem: DisplayItemConfig = DisplayItemConfig(),

    @Comment("All purchasable items within this category.")
    var items: MutableMap<Int, ShopItemConfig> = mutableMapOf()
)

@Configuration
data class DisplayItemConfig(
    var material: String = "PAPER",
    var name: String? = "&eRanks",
    var description: MutableList<String> = mutableListOf("Purchase your permanent ranks here!")
)

@Configuration
data class ShopItemConfig(
    @Comment("The position of this item in the category menu (row|column).")
    var slot: String = "2|2",

    @Comment("The display name of the item.")
    var name: String = "Bronze Rank",

    @Comment("Lore/description of the item.")
    var description: MutableList<String> = mutableListOf(
        "&fReceive the &6[Bronze] &ftag!",
        "&fPerks:",
        "&e/bronze"
    ),

    @Comment("Price in the currency for this item.")
    var price: Double = 100.0,

    @Comment("The Minecraft material to display for this item.")
    var material: String = "PAPER",

    @Comment(
        "A permission or LuckPerms group to check if the player already owns this item.",
        "Example: 'group.bronze' or 'ranks.bronze'"
    )
    var permissionCheck: String? = null,

    @Comment(
        "Optional list of higher ranks that block purchase if owned.",
        "Example: ['group.silver', 'group.gold']"
    )
    var higherRanks: MutableList<String> = mutableListOf(),

    @Comment(
        "Commands to run when the item is purchased.",
        "Use %player% for player name placeholders."
    )
    var purchaseCommands: MutableList<String> = mutableListOf("/lp user %player% parent add bronze")
)

@Configuration
data class GlobalInfoItemConfig(
    var material: String = "BOOK",
    var menu: String = "main",
    var slot: String = "2|5",
    var name: String = "&bInformation",
    var description: MutableList<String> = mutableListOf(
        "&7Earn %currency% &7through &avoting &7or buying at &e/store!",
        "&7Spend them here for awesome &e&lrewards!"
    )
)
