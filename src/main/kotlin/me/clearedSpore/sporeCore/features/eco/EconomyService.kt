package me.clearedSpore.sporeCore.features.eco

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.database.DatabaseManager
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

    fun add(user: User, amount: Double, reason: String = "", shouldSave: Boolean = true) {
        user.balance += amount
        user.logEconomy(EcoAction.ADDED, amount, reason)
        if(shouldSave) {
            UserManager.save(user)
        }

    }

    fun remove(user: User, amount: Double, reason: String = "") {
        user.balance -= amount
        user.logEconomy(EcoAction.REMOVED, amount, reason)
        UserManager.save(user)
    }


    fun format(amount: Double): String {
        val absAmount = kotlin.math.abs(amount)
        val df = java.text.DecimalFormat("#.##")

        return when {
            absAmount >= 1_000_000_000_000 -> df.format(amount / 1_000_000_000_000) + "t"
            absAmount >= 1_000_000_000 -> df.format(amount / 1_000_000_000) + "b"
            absAmount >= 1_000_000 -> df.format(amount / 1_000_000) + "m"
            absAmount >= 1_000 -> df.format(amount / 1_000) + "k"
            else -> df.format(amount)
        }
    }


    fun set(user: User, amount: Double, reason: String = "") {
        user.balance = amount
        user.logEconomy(EcoAction.SET, amount, reason)
        UserManager.save(user)
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
