package me.clearedSpore.sporeCore.features.eco.`object`

import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red


enum class EcoAction {
    ADDED, REMOVED, SET;

    fun format(amount: Double): String {
        val formattedAmount = "%,.2f".format(amount)
        return when (this) {
            ADDED -> "+$formattedAmount".green()
            REMOVED -> "-$formattedAmount".red()
            SET -> formattedAmount.green()
        }
    }
}
