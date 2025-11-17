package me.clearedSpore.sporeCore.features.currency.`object`

import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.features.currency.CurrencySystemService


enum class CreditAction {
    ADDED, REMOVED, SET, SPENT;

    val currency = CurrencySystemService.config.currencySettings.pluralName

    fun format(amount: Double): String {
        val formattedAmount = "%,.2f".format(amount)
        return when (this) {
            ADDED -> "+$formattedAmount $currency".green()
            REMOVED -> "-$formattedAmount $currency".red()
            SET -> formattedAmount.green()
            SPENT -> "-$formattedAmount".red()
        }
    }
}
