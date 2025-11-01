package me.clearedSpore.sporeCore.hook

import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.currency.CurrencySystemService
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

        val args = params.lowercase().split("_")

        return when {
            // %sporecore_balance_raw%
            params.equals("balance_raw", ignoreCase = true) -> user.balance.toString()

            // %sporecore_balance_formatted%
            params.equals("balance_formatted", ignoreCase = true) ->
                EconomyService.format(user.balance, BalanceFormat.COMPACT).toString()

            // %sporecore_balance_decimal%
            params.equals("balance_decimal", ignoreCase = true) ->
                EconomyService.format(user.balance, BalanceFormat.DECIMAL).toString()

            // %sporecore_homes%
            params.equals("homes", ignoreCase = true) ->
                SporeCore.instance.homeService.getAllHomes(user).size.toString()

            // %sporecore_kit_timeleft_<kitName>%
            args.size >= 3 && args[0] == "kit" && args[1] == "timeleft" -> {
                val kitName = args.drop(2).joinToString("_")
                val remaining = user.getKitCooldownRemaining(kitName)
                if (remaining <= 0) "Ready" else TimeUtil.formatDuration(remaining)
            }

            //%sporecore_<currencyName>_balance_<format>%
            args.size >= 2 && args[1].equals("balance", ignoreCase = true) -> {
                val currencyToken = args[0].lowercase()

                val settings = CurrencySystemService.config.currencySettings
                val matchesCurrency = currencyToken == settings.pluralName.lowercase()
                        || currencyToken == settings.singularName.lowercase()

                if (!matchesCurrency) {
                    null
                } else {
                    val formatToken = args.getOrNull(2)?.lowercase()
                    val formatType = when (formatToken) {
                        "plain" -> BalanceFormat.PLAIN
                        "decimal" -> BalanceFormat.DECIMAL
                        "compact" -> BalanceFormat.COMPACT
                        null -> runCatching { BalanceFormat.valueOf(settings.balanceFormat.uppercase()) }
                            .getOrDefault(BalanceFormat.PLAIN)
                        else -> runCatching { BalanceFormat.valueOf(settings.balanceFormat.uppercase()) }
                            .getOrDefault(BalanceFormat.PLAIN)
                    }

                    val balance = CurrencySystemService.getBalance(user)
                    CurrencySystemService.format(balance, formatType)
                }
            }

            else -> null
        }
    }
}