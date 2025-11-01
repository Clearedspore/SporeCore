package me.clearedSpore.sporeCore.currency.`object`

import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red


enum class CreditAction {
    ADDED, REMOVED, SET, SPENT;

    fun format(amount: Double): String {
        val formattedAmount = "%,.2f".format(amount)
        return when (this) {
            ADDED -> "+$formattedAmount".green()
            REMOVED -> "-$formattedAmount".red()
            SET -> formattedAmount.green()
            SPENT -> "-$formattedAmount".red()
        }
    }
}
