package me.clearedSpore.sporeCore.acf.selector

data class DoubleRange(
    val min: Double?,
    val max: Double?
) {
    fun contains(value: Double): Boolean {
        if (min != null && value < min) return false
        if (max != null && value > max) return false
        return true
    }
}

fun parseDoubleRange(input: String): DoubleRange? {
    val parts = input.split("..")
    return when (parts.size) {
        1 -> {
            val value = parts[0].toDoubleOrNull() ?: return null
            DoubleRange(value, value)
        }

        2 -> {
            val min = parts[0].takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            val max = parts[1].takeIf { it.isNotEmpty() }?.toDoubleOrNull()
            DoubleRange(min, max)
        }

        else -> null
    }
}
