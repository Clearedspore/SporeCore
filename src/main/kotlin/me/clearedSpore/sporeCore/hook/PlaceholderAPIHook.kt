package me.clearedSpore.sporeCore.hook

import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.eco.`object`.BalanceFormat
import me.clearedSpore.sporeCore.user.UserManager
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer


class PlaceholderAPIHook() : PlaceholderExpansion() {

    override fun getIdentifier() = "sporecore"
    override fun getAuthor() = "ClearedSpore"
    override fun getVersion() = "1.0"

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return null

        val user = UserManager.get(player) ?: return null

        return when (params) {
            "balance_raw" -> {
                return user.balance.toString()
            }

            "balance_formatted" -> {
                val balance = EconomyService.format(user.balance, BalanceFormat.COMPACT).toString()
                return balance
            }

            "balance_decimal"-> {
                val balance = EconomyService.format(user.balance, BalanceFormat.DECIMAL).toString()
                return balance
            }

            else -> null
        }
    }
}