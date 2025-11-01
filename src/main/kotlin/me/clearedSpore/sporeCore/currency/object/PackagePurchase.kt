package me.clearedSpore.sporeCore.currency.`object`


data class PackagePurchase(
    val packageName: String,
    val amount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)