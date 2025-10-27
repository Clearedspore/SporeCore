package me.clearedSpore.sporeCore.hook

import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.eco.`object`.BalanceFormat
import me.clearedSpore.sporeCore.user.UserManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player


class PlaceholderAPIHook() : PlaceholderExpansion() {

    override fun getIdentifier(): String = "sporecore"
    override fun getAuthor(): String = "ClearedSpore"
    override fun getVersion(): String = "1.0"
    override fun persist() = true

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        val user = UserManager.get(player) ?: return null

        return when (params.lowercase()) {
            "balance_raw" -> return user.balance.toString()

            "balance_formatted" -> {
                val balance = EconomyService.format(user.balance, BalanceFormat.COMPACT).toString()
                return balance
            }

            "balance_decimal" -> {
                val balance = EconomyService.format(user.balance, BalanceFormat.DECIMAL).toString()
                return balance
            }

            else -> null
        }
    }
}