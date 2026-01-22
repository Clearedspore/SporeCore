package me.clearedSpore.sporeCore.features.currency

import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.commands.currency.CurrencyCommand
import me.clearedSpore.sporeCore.commands.currency.CurrencyShopCommand
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.currency.config.*
import me.clearedSpore.sporeCore.features.currency.`object`.CreditAction
import me.clearedSpore.sporeCore.features.currency.`object`.CreditLog
import me.clearedSpore.sporeCore.features.currency.`object`.PackagePurchase
import me.clearedSpore.sporeCore.features.eco.`object`.BalanceFormat
import me.clearedSpore.sporeCore.features.setting.impl.CurrencyLogsSetting
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object CurrencySystemService {

    lateinit var config: CurrencySystemConfig
        private set

    private val categories = mutableMapOf<String, ShopCategoryConfig>()
    val currencyName: String
        get() = config.currencySettings.pluralName

    fun initialize() {
        loadConfig()
        loadCategories()
        Logger.info("[$currencyName] Currency system initialized successfully with ${categories.size} categories.")
    }

    private fun loadConfig(): CurrencySystemConfig {
        val dataFolder = SporeCore.instance.dataFolder
        val fileName = SporeCore.instance.coreConfig.features.currency.configFile
        val configFile = File(dataFolder, fileName).toPath()
        return try {
            config = YamlConfigurations.update(configFile, CurrencySystemConfig::class.java)
            Logger.info("[$currencyName] Loaded ${fileName} successfully.")
            config
        } catch (ex: ConfigurationException) {
            Logger.error("[$currencyName] Invalid config detected — defaults applied.")
            ex.printStackTrace()
            config = CurrencySystemConfig()
            config
        }
    }

    private fun loadCategories() {
        categories.clear()
        val shopConfig = config.shop

        if (shopConfig.categories.isEmpty()) {
            Logger.warn("[$currencyName] No categories found! Adding default 'Ranks' category.")
            val defaultCategory = ShopCategoryConfig(
                name = "Ranks",
                slot = "2|6",
                displayItem = DisplayItemConfig(
                    material = "PAPER",
                    name = "&eRanks",
                    description = mutableListOf("Purchase your permanent ranks here!")
                )
            )
            shopConfig.categories["ranks"] = defaultCategory
        }

        shopConfig.categories.forEach { (key, category) ->
            val lowerKey = key.lowercase()
            categories[lowerKey] = category

            if (!shopConfig.menuSettings.categories.containsKey(lowerKey)) {
                Logger.info("[$currencyName] No menu settings found for category '${category.name}', adding default settings.")
                shopConfig.menuSettings.categories[lowerKey] = MenuRowConfig(rows = 5, fillItems = true)
            }

            Logger.info("[$currencyName] Loaded category '${category.name}' with ${category.items.size} items.")
        }
    }

    fun reload() {
        initialize()

        val plugin = SporeCore.instance
        val commandManager = plugin.commandManager

        commandManager.unregisterCommand(CurrencyCommand())
        commandManager.unregisterCommand(CurrencyShopCommand())

        val singular = config.currencySettings.singularName.lowercase()
        val plural = config.currencySettings.pluralName.lowercase()
        val aliases = listOf("currency", singular, plural)

        for (alias in aliases.distinct()) {
            commandManager.commandReplacements.addReplacement("currencyalias", alias)
            commandManager.registerCommand(CurrencyCommand())
        }

        val shopAliases = config.currencySettings.shopCommand
        if (shopAliases.isNotEmpty()) {
            for (alias in shopAliases.distinct()) {
                commandManager.commandReplacements.addReplacement("currencyshopalias", alias)
                commandManager.registerCommand(CurrencyShopCommand())
            }
        }

        Logger.info("[$plural] Currency system reloaded. Registered aliases: ${aliases.joinToString(", ")}")
    }

    fun getMenuSettingsFor(menuName: String): MenuRowConfig {
        val settings = config.shop.menuSettings
        return if (menuName.equals("main", ignoreCase = true)) {
            settings.main
        } else {
            settings.categories[menuName.lowercase()] ?: MenuRowConfig()
        }
    }


    fun addBalance(sender: CommandSender, user: User, amount: Double, reason: String) {
        user.credits = user.credits + amount
        user.logCredit(CreditAction.ADDED, amount, reason)
        UserManager.save(user)
        broadcastLog(sender, user, amount, CreditAction.ADDED, reason)
    }

    fun removeBalance(sender: CommandSender, user: User, amount: Double, reason: String) {
        user.credits -= amount
        user.logCredit(CreditAction.REMOVED, amount, reason)

        broadcastLog(sender, user, amount, CreditAction.REMOVED, reason)

        UserManager.save(user)
    }

    fun removeBalance(user: User, amount: Double, reason: String) {
        user.credits -= amount
        user.logCredit(CreditAction.REMOVED, amount, reason)

        user.creditsSpent.add(CreditLog(CreditAction.SPENT, amount, reason, System.currentTimeMillis()))
        broadcastLog(Bukkit.getConsoleSender(), user, amount, CreditAction.SPENT, reason)

        UserManager.save(user)
    }

    fun setBalance(sender: CommandSender, user: User, amount: Double, reason: String) {
        user.credits = amount
        user.logCredit(CreditAction.SET, amount, reason)
        UserManager.save(user)
        broadcastLog(sender, user, amount, CreditAction.SET, reason)
    }

    fun broadcastLog(
        sender: CommandSender,
        targetUser: User,
        amount: Double,
        action: CreditAction,
        reason: String? = null
    ) {
        val settings = config.currencySettings
        if (!settings.broadcastStaff) return

        val currencyColor = settings.currencyColor
        val currencyName = settings.pluralName
        val formattedAmount = format(amount)
        val senderName = if (sender is Player) sender.name else "Console"
        val targetName = targetUser.playerName.ifEmpty { "Unknown" }

        for (player in Bukkit.getOnlinePlayers()) {
            val playerUser = UserManager.get(player) ?: continue
            if (!player.hasPermission(Perm.CURRENCY_NOTIFY) || !playerUser.getSettingOrDefault(CurrencyLogsSetting())) continue

            val message = when (action) {
                CreditAction.ADDED ->
                    "&c[$currencyName]&r ".blue() +
                            "$targetName ".white() +
                            "received ".blue() +
                            "$currencyColor$formattedAmount ".translate() +
                            "from ".blue() +
                            senderName.white() +
                            (if (!reason.isNullOrBlank()) " for ".blue() + "&e$reason".translate() else "")

                CreditAction.REMOVED ->
                    "&c[$currencyName]&r ".blue() +
                            "$senderName ".white() +
                            "removed ".blue() +
                            "$currencyColor$formattedAmount ".translate() +
                            "from ".blue() +
                            targetName.white() +
                            (if (!reason.isNullOrBlank()) " for ".blue() + "&e$reason".translate() else "")

                CreditAction.SET ->
                    "&c[$currencyName]&r ".blue() +
                            "$senderName ".white() +
                            "has set ".blue() +
                            targetName.white() +
                            "'s balance to ".blue() +
                            "$currencyColor$formattedAmount ".translate() +
                            (if (!reason.isNullOrBlank()) " for ".blue() + "&e$reason".translate() else "")

                CreditAction.SPENT ->
                    "&c[$currencyName]&r ".blue() +
                            "$targetName ".white() +
                            "has spent ".blue() +
                            "$currencyColor$formattedAmount ".translate()
            }

            player.sendMessage(message)
        }
    }


    fun hasBalance(user: User, amount: Double): Boolean {
        return user.credits >= amount
    }

    fun getBalance(user: User): Double {
        return user.credits
    }

    fun hasPermissionOrRank(player: Player, item: ShopItemConfig): Boolean {
        if (item.permissionCheck?.let { player.hasPermission(it) } == true) return true
        if (item.higherRanks.any { player.hasPermission(it) }) return true
        return false
    }

    fun format(amount: Double, formatOverride: BalanceFormat? = null): String {
        val settings = config.currencySettings
        val includeName = settings.nameAfterAmount
        val formatToUse = formatOverride ?: settings.balanceFormat

        val formattedAmount = when (formatToUse) {
            BalanceFormat.PLAIN -> formatPlain(amount, settings)
            BalanceFormat.DECIMAL -> formatDecimal(amount, settings)
            BalanceFormat.COMPACT -> formatCompact(amount, settings)
            else -> formatPlain(amount, settings)
        }.white()


        val space = if (settings.spaceAfterSymbol) " " else ""

        val base = if (settings.symbolBeforeAmount)
            "${settings.symbol}$space$formattedAmount"
        else
            "$formattedAmount$space${settings.symbol}"

        if (!includeName) return base.translate()

        val coloredCurrency = "${settings.currencyColor}${settings.pluralName}"

        return if (settings.nameAfterAmount) {
            "$base §f$coloredCurrency".translate()
        } else {
            "$coloredCurrency §f$base".translate()
        }
    }

    private fun formatPlain(amount: Double, settings: CurrencySettingConfig): String {
        val pattern = if (settings.useThousandSeparator) "#,###" else "####"
        val df = DecimalFormat(pattern)
        df.maximumFractionDigits = settings.decimalDigits
        df.minimumFractionDigits = settings.decimalDigits
        return df.format(amount)
    }

    private fun formatDecimal(amount: Double, settings: CurrencySettingConfig): String {
        val pattern = buildString {
            append(if (settings.useThousandSeparator) "#,##0" else "0")
            if (settings.decimalDigits > 0) append("." + "0".repeat(settings.decimalDigits))
        }
        val df = DecimalFormat(pattern)
        return df.format(amount)
    }

    fun parseSlot(slotString: String): Pair<Int, Int> {
        val parts = slotString.split("|")
        if (parts.size != 2) return 1 to 1
        val row = parts[0].toIntOrNull() ?: 1
        val column = parts[1].toIntOrNull() ?: 1
        return row to column
    }


    private fun formatCompact(amount: Double, settings: CurrencySettingConfig): String {
        val absAmount = abs(amount)
        val suffixes = listOf("", "k", "m", "b", "t", "q", "b")
        val suffixIndex = if (absAmount == 0.0) 0 else floor(log10(absAmount) / 3)
            .toInt()
            .coerceAtMost(suffixes.lastIndex)

        val shortValue = amount / 1000.0.pow(suffixIndex.toDouble())
        val df = DecimalFormat(if (settings.decimalDigits > 0) "#.${"0".repeat(settings.decimalDigits)}" else "#")
        val base = df.format(shortValue)

        return base + suffixes[suffixIndex]
    }

    fun parsePlaceholders(
        input: String,
        player: Player? = null,
        user: User? = null,
        packageName: String? = null,
        cost: Double? = null
    ): String {
        var text = input

        val resolvedUser = user ?: player?.let { UserManager.get(it) }

        if (player != null) {
            text = text.replace("%player%", player.name)
        }

        if (resolvedUser != null) {
            val formattedBalance = format(resolvedUser.credits)
            text = text.replace("%balance%", formattedBalance)
        }

        text = text.replace("%currency%", config.currencySettings.pluralName)
        text = text.replace("%singular_currency%", config.currencySettings.singularName)
        text = text.replace("%symbol%", config.currencySettings.symbol)

        if (packageName != null) {
            text = text.replace("%package_name%", packageName)
        }

        if (cost != null) {
            val formattedCost = format(cost)
            text = text.replace("%costs%", formattedCost)
        }

        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text)
        }

        return text.translate()
    }


    fun handlePurchase(player: Player, item: ShopItemConfig) {
        val user = UserManager.get(player)
        if (user == null) {
            player.userFail()
            return
        }

        item.permissionCheck?.let { perm ->
            if (player.hasPermission(perm)) {
                player.sendMessage(
                    parsePlaceholders(
                        "You already own '%package_name%'.",
                        player,
                        user,
                        packageName = item.name
                    ).red()
                )
                return
            }
        }

        if (item.higherRanks.any { player.hasPermission(it) }) {
            player.sendMessage(
                parsePlaceholders(
                    "You already have a higher rank. You cannot purchase '%package_name%'.",
                    player,
                    user,
                    packageName = item.name
                ).red()
            )
            return
        }

        if (!hasBalance(user, item.price)) {
            player.sendMessage(
                parsePlaceholders(
                    "You do not have enough %singular_currency% to buy '%package_name%'.",
                    player,
                    user,
                    packageName = item.name,
                    cost = item.price
                ).red()
            )
            return
        }

        removeBalance(user, item.price, "Bought the ${item.name}" + " package")
        val db = DatabaseManager.getServerData()
        db.packagePurchases.add(PackagePurchase(item.name, 1))
        db.save(DatabaseManager.getServerCollection())

        item.purchaseCommands.forEach { cmd ->
            val command = parsePlaceholders(cmd, player, user, packageName = item.name, cost = item.price)
            val cleanCommand = if (command.startsWith("/")) command.drop(1) else command
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cleanCommand)
        }

        format(item.price)
        player.sendMessage(
            parsePlaceholders(
                "You successfully purchased '%package_name%".green() + "' for %costs%!".green(),
                player,
                user,
                packageName = item.name,
                cost = item.price
            ).green()
        )


        config.currencySettings.broadcastMessages.forEach { msg ->
            Bukkit.broadcastMessage(
                parsePlaceholders(
                    msg,
                    player,
                    user,
                    packageName = item.name,
                    cost = item.price
                )
            )
        }
    }

    fun topSpenders(
        lastMonthOnly: Boolean = false,
        limit: Int = 10
    ): CompletableFuture<List<Pair<OfflinePlayer, Double>>> {
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val uuids = UserManager.getAllStoredUUIDsFromDB().distinct()

        val spentFutures = uuids.map { uuid ->
            UserManager.getTotalCreditsSpent(uuid).handle { totalSpent, ex ->
                if (ex != null) {
                    Logger.warn("Failed to fetch total credits spent for $uuid: ${ex.message}")
                    null
                } else {
                    val filteredSpent = if (lastMonthOnly) {
                        UserManager.get(uuid)?.creditLogs
                            ?.filter { it.action == CreditAction.SPENT && it.timestamp >= cutoff }
                            ?.sumOf { it.amount } ?: 0.0
                    } else totalSpent ?: 0.0

                    if (filteredSpent > 0.0) Bukkit.getOfflinePlayer(uuid) to filteredSpent else null
                }
            }
        }

        return CompletableFuture.allOf(*spentFutures.toTypedArray()).thenApply {
            spentFutures.mapNotNull { it.getNow(null) }
                .sortedByDescending { it.second }
                .take(limit)
        }
    }


    fun topBoughtPackages(lastMonthOnly: Boolean = false, limit: Int = 10): List<Pair<String, Int>> {
        val db = DatabaseManager.getServerData()
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000

        return db.packagePurchases
            .filter { !lastMonthOnly || it.timestamp >= cutoff }
            .groupingBy { it.packageName }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }
    }


    fun topCredits(limit: Int = 10): CompletableFuture<List<Pair<OfflinePlayer, Double>>> {
        val uuids = UserManager.getAllStoredUUIDsFromDB().distinct()

        val creditFutures = uuids.map { uuid ->
            UserManager.getCredits(uuid).handle { credits, ex ->
                if (ex != null) {
                    Logger.warn("Failed to fetch credits for $uuid: ${ex.message}")
                    null
                } else {
                    Bukkit.getOfflinePlayer(uuid) to credits
                }
            }
        }

        return CompletableFuture.allOf(*creditFutures.toTypedArray()).thenApply {
            creditFutures.mapNotNull { it.getNow(null) }
                .filter { (_, credits) -> credits != null && credits > 0.0 }
                .map { it.first to it.second!! }
                .sortedByDescending { it.second }
                .take(limit)
        }
    }


    fun getCategory(key: String): ShopCategoryConfig? = categories[key.lowercase()]
    fun getCategories(): Map<String, ShopCategoryConfig> = categories.toMap()
}
