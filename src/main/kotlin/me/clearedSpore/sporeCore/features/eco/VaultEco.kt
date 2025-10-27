package me.clearedSpore.sporeCore.features.eco

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.eco.`object`.EcoAction
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Tasks
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer


class VaultEco : Economy {

    var config = SporeCore.instance.coreConfig.economy

    override fun isEnabled(): Boolean {
        return config.enabled
    }

    override fun getName(): String? {
        return config.name
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun fractionalDigits(): Int {
        return config.digits
    }

    override fun format(amount: Double): String {
        val cfg = config

        val pattern = buildString {
            append(if (cfg.useThousandSeparator) "#,##0" else "0")
            if (cfg.digits > 0) append("." + "0".repeat(cfg.digits))
        }

        val formatter = java.text.DecimalFormat(pattern)
        val formattedAmount = formatter.format(amount)

        val currencyName = if (amount == 1.0) cfg.singularName else cfg.pluralName

        val symbolPart = if (cfg.symbol.isNotEmpty()) {
            if (cfg.spaceAfterSymbol) "${cfg.symbol} " else cfg.symbol
        } else ""

        return if (cfg.symbolBeforeAmount) {
            "$symbolPart$formattedAmount $currencyName"
        } else {
            "$formattedAmount ${symbolPart}$currencyName"
        }
    }


    override fun currencyNamePlural(): String {
        return config.pluralName
    }

    override fun currencyNameSingular(): String {
        return config.singularName
    }

    override fun hasAccount(playerName: String): Boolean {
        return true
    }

    override fun hasAccount(offlinePlayer: OfflinePlayer): Boolean {
        return true
    }

    override fun hasAccount(playerName: String, worldName: String?): Boolean {
        return true
    }

    override fun hasAccount(offlinePlayer: OfflinePlayer, worldName: String?): Boolean {
        return true
    }

    override fun getBalance(offlinePlayer: String): Double {
        val player = Bukkit.getOfflinePlayer(offlinePlayer)
        return getBalance(player)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        val user = UserManager.get(player.uniqueId)
        if (user == null) {
            Logger.error("Failed to load data for ${player.name}")
            return 0.0
        }
        return user.balance
    }


    override fun getBalance(playerName: String, worldName: String?): Double = getBalance(playerName)

    override fun getBalance(offlinePlayer: OfflinePlayer, worldName: String?): Double = getBalance(offlinePlayer)

    override fun has(playerName: String, amount: Double): Boolean = has(playerName, amount)

    override fun has(player: OfflinePlayer, amount: Double): Boolean = getBalance(player) >= amount

    override fun has(playerName: String, worldName: String, amount: Double): Boolean = has(playerName, amount)

    override fun has(offlinePlayer: OfflinePlayer, worldName: String, amount: Double): Boolean =
        has(offlinePlayer, amount)

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        val player = Bukkit.getOfflinePlayer(playerName)
        return withdrawPlayer(player, amount)
    }

    override fun withdrawPlayer(
        offlinePlayer: OfflinePlayer,
        amount: Double
    ): EconomyResponse {
        val user = UserManager.get(offlinePlayer.uniqueId)
        if (user == null) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Failed to load user!")
        }

        if (user.balance < amount) {
            return EconomyResponse(0.0, user.balance, ResponseType.FAILURE, "Insufficient balance!")
        }

        user.balance -= amount
        Tasks.runAsync {
            user.logEconomy(EcoAction.REMOVED, amount, "Vault withdrawal by a different plugin")
            UserManager.save(user)
            EconomyService.logConsole(user.playerName, EcoAction.REMOVED, amount, "Vault withdrawal by a different plugin")
        }

        return EconomyResponse(amount, user.balance, ResponseType.SUCCESS, null)
    }

    override fun withdrawPlayer(
        playerName: String,
        worldName: String,
        amount: Double
    ): EconomyResponse = withdrawPlayer(playerName, amount)

    override fun withdrawPlayer(
        offlinePlayer: OfflinePlayer,
        worldName: String,
        amount: Double
    ): EconomyResponse = withdrawPlayer(offlinePlayer, amount)

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse = depositPlayer(playerName, amount)

    override fun depositPlayer(
        offlinePlayer: OfflinePlayer,
        amount: Double
    ): EconomyResponse {
        val user = UserManager.get(offlinePlayer.uniqueId)
        if (user == null) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Failed to load user!")
        }

        user.balance += amount
        Tasks.runAsync {
            user.logEconomy(EcoAction.ADDED, amount, "Vault deposit by a different plugin")
            UserManager.save(user)
            EconomyService.logConsole(user.playerName, EcoAction.ADDED, amount, "Vault withdrawal by a different plugin")
        }

        return EconomyResponse(amount, user.balance, ResponseType.SUCCESS, null)
    }

    override fun depositPlayer(
        playerName: String,
        worldName: String,
        amount: Double
    ): EconomyResponse = depositPlayer(playerName, amount)

    override fun depositPlayer(
        offlinePlayer: OfflinePlayer,
        worldName: String,
        amount: Double
    ): EconomyResponse = depositPlayer(offlinePlayer, amount)

    override fun createBank(p0: String?, p1: String?): EconomyResponse? = null
    override fun createBank(p0: String?, p1: OfflinePlayer?): EconomyResponse? = null
    override fun deleteBank(p0: String?): EconomyResponse? = null
    override fun bankBalance(p0: String?): EconomyResponse? = null
    override fun bankHas(p0: String?, p1: Double): EconomyResponse? = null
    override fun bankWithdraw(p0: String?, p1: Double): EconomyResponse? = null
    override fun bankDeposit(p0: String?, p1: Double): EconomyResponse? = null
    override fun isBankOwner(p0: String?, p1: String?): EconomyResponse? = null
    override fun isBankOwner(p0: String?, p1: OfflinePlayer?): EconomyResponse? = null
    override fun isBankMember(p0: String?, p1: String?): EconomyResponse? = null
    override fun isBankMember(p0: String?, p1: OfflinePlayer?): EconomyResponse? = null
    override fun getBanks(): List<String?>? = null
    override fun createPlayerAccount(p0: String?): Boolean = false
    override fun createPlayerAccount(p0: OfflinePlayer?): Boolean = false
    override fun createPlayerAccount(p0: String?, p1: String?): Boolean = false
    override fun createPlayerAccount(p0: OfflinePlayer?, p1: String?): Boolean = false
}