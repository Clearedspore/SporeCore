package me.clearedSpore.sporeCore.features.eco

import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.eco.`object`.BalanceFormat
import me.clearedSpore.sporeCore.features.eco.`object`.EcoAction
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

object EconomyService {

    @Volatile
    private var ready: Boolean = false

    fun isReady(): Boolean = ready

    fun reloadAsync(): CompletableFuture<Void> = CompletableFuture.runAsync {
        val startTime = System.currentTimeMillis()
        Logger.info("Loading EconomyService...")

        val plugin = SporeCore.instance
        val ecoConfig = plugin.coreConfig.economy

        if (!ecoConfig.enabled) {
            ready = false
            Logger.info("Economy is disabled in config.")
            return@runAsync
        }

        val provider = Bukkit.getServicesManager().getRegistration(Economy::class.java)?.provider
        if (provider != null) {
            ready = true
            Logger.info("Vault economy hooked successfully.")
        } else {
            ready = false
            Logger.warn("Vault economy not found! Economy commands will still work internally.")
        }

        val elapsed = System.currentTimeMillis() - startTime
        Logger.info("EconomyService loaded in ${elapsed}ms")
    }

    fun parseAmount(input: String): Double? {
        val cleaned = input.trim().lowercase()
        return try {
            when {
                cleaned.endsWith("k") -> cleaned.dropLast(1).toDouble() * 1_000
                cleaned.endsWith("m") -> cleaned.dropLast(1).toDouble() * 1_000_000
                cleaned.endsWith("b") -> cleaned.dropLast(1).toDouble() * 1_000_000_000
                else -> cleaned.toDouble()
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun logConsole(userName: String, action: EcoAction, amount: Double, reason: String = "") {
        val shouldLog = SporeCore.instance.coreConfig.economy.logging
        if (!shouldLog) return

        val formatter = java.time.format.DateTimeFormatter.ofPattern("MM/dd/yy HH:mm")
        val timestamp = java.time.Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(java.time.ZoneId.systemDefault())
            .format(formatter)

        val amountStr = action.format(amount)
        val reasonText = if (reason.isNotBlank()) " | ${reason}" else ""

        val message = "§7[§6Economy§7] §f($userName) §7| $amountStr$reasonText".white() + " [${timestamp}]".gray()

        Bukkit.getConsoleSender().sendMessage(message)
    }

    fun add(user: User, amount: Double, reason: String = "", shouldSave: Boolean = true) {
        user.balance += amount
        user.logEconomy(EcoAction.ADDED, amount, reason)
        if (shouldSave) {
            UserManager.save(user)
        }

        logConsole(user.playerName, EcoAction.ADDED, amount, reason)
    }


    fun remove(user: User, amount: Double, reason: String = "") {
        user.balance -= amount
        user.logEconomy(EcoAction.REMOVED, amount, reason)
        UserManager.save(user)
        logConsole(user.playerName, EcoAction.REMOVED, amount, reason)
    }

    fun set(user: User, amount: Double, reason: String = "", shouldSave: Boolean = true) {
        user.balance = amount
        user.logEconomy(EcoAction.SET, amount, reason)
        if (shouldSave) {
            UserManager.save(user)
        }
        logConsole(user.playerName, EcoAction.SET, amount, reason)
    }


    fun format(amount: Double, formatOverride: BalanceFormat? = null): String {
        val cfg = SporeCore.instance.coreConfig.economy
        val formatToUse = formatOverride ?: cfg.balanceFormat
        val digits = cfg.digits.coerceIn(0, 10)

        val pattern = buildString {
            append(if (cfg.useThousandSeparator) "#,##0" else "0")
            if (digits > 0) append("." + "0".repeat(digits))
        }
        val formatter = java.text.DecimalFormat(pattern)

        val (value, suffix) = when {
            kotlin.math.abs(amount) >= 1_000_000_000_000 -> amount / 1_000_000_000_000 to "T"
            kotlin.math.abs(amount) >= 1_000_000_000 -> amount / 1_000_000_000 to "B"
            kotlin.math.abs(amount) >= 1_000_000 -> amount / 1_000_000 to "M"
            kotlin.math.abs(amount) >= 1_000 -> amount / 1_000 to "K"
            else -> amount to ""
        }

        val formattedBase = when (formatToUse) {
            BalanceFormat.PLAIN -> amount.toLong().toString()
            BalanceFormat.DECIMAL -> formatter.format(amount)
            BalanceFormat.COMPACT -> formatter.format(value) + suffix
        }

        val currencyName = if (amount == 1.0) cfg.singularName else cfg.pluralName

        val symbolPart = if (cfg.symbol.isNotEmpty()) {
            if (cfg.spaceAfterSymbol) "${cfg.symbol} " else cfg.symbol
        } else ""

        return if (cfg.symbolBeforeAmount) {
            "$symbolPart$formattedBase $currencyName".trim()
        } else {
            "$formattedBase ${symbolPart}$currencyName".trim()
        }
    }



    fun set(user: User, amount: Double, reason: String = "") {
        user.balance = amount
        user.logEconomy(EcoAction.SET, amount, reason)
        UserManager.save(user)
    }

    fun top(limit: Int = 10): CompletableFuture<List<Pair<OfflinePlayer, Double>>> {
        val uuids = UserManager.getAllStoredUUIDsFromDB().distinct()

        val balanceFutures = uuids.map { uuid ->
            UserManager.getBalance(uuid).handle { balance, ex ->
                if (ex != null) {
                    Logger.warn("Failed to fetch balance for $uuid: ${ex.message}")
                    null
                } else {
                    Bukkit.getOfflinePlayer(uuid) to balance
                }
            }
        }

        return CompletableFuture.allOf(*balanceFutures.toTypedArray()).thenApply {
            balanceFutures.mapNotNull { it.getNow(null) }
                .filter { (_, balance) -> balance != null && balance > 0.0 }
                .map { it.first to it.second!! }
                .sortedByDescending { it.second }
                .take(limit)
        }
    }
}
