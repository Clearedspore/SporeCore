package me.clearedSpore.sporeCore.user

import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.eco.`object`.EcoAction
import me.clearedSpore.sporeCore.features.eco.`object`.EconomyLog
import me.clearedSpore.sporeCore.features.homes.`object`.Home
import me.clearedSpore.sporeCore.user.settings.Setting
import me.clearedSpore.sporeCore.database.util.DocReader
import me.clearedSpore.sporeCore.database.util.DocWriter
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.dizitart.no2.collection.NitriteCollection
import org.dizitart.no2.filters.FluentFilter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture

data class User(
    var uuidStr: String = UUID.randomUUID().toString(),
    var playerName: String = "",
    var hasJoinedBefore: Boolean = false,
    var firstJoin: String? = null,
    var balance: Double = 0.0,
    var pendingMessages: MutableList<String> = mutableListOf(),
    var pendingPayments: MutableMap<String, Double> = mutableMapOf(),
    var playerSettings: MutableMap<String, Boolean> = mutableMapOf(),
    var homes: MutableList<Home> = mutableListOf(),
    var economyLogs: MutableList<EconomyLog> = mutableListOf(),
    var kitCooldowns: MutableMap<String, Long> = mutableMapOf(),
    var lastJoin: Long? = null,
    var totalPlaytime: Long = 0L,
    var playtimeHistory: MutableList<Pair<Long, Long>> = mutableListOf()
) {
    val uuid: UUID get() = UUID.fromString(uuidStr)
    val player: Player? get() = Bukkit.getPlayer(uuid)

    fun toDocument() = DocWriter()
        .put("uuidStr", uuidStr)
        .put("playerName", playerName)
        .put("hasJoinedBefore", hasJoinedBefore)
        .put("firstJoin", firstJoin)
        .putList("pendingMessages", pendingMessages)
        .putMap("pendingPayments", pendingPayments)
        .putMap("playerSettings", playerSettings)
        .putDocuments("homes", homes.map { it.toDocument() })
        .putDocuments("economyLogs", economyLogs.map { it.toDocument() })
        .putDouble("balance", balance)
        .putMap("kitCooldowns", kitCooldowns)
        .putLong("lastJoin", lastJoin ?: 0L)
        .putLong("totalPlaytime", totalPlaytime)
        .putList("playtimeHistory", playtimeHistory.map { "${it.first}:${it.second}" })
        .build()

    companion object {

        fun load(uuid: UUID, collection: NitriteCollection): User? {
            val docRaw = collection.find(FluentFilter.where("uuidStr").eq(uuid.toString())).firstOrNull()
                ?: return null

            val doc = DocReader(docRaw)
            return User(
                uuidStr = doc.string("uuidStr") ?: uuid.toString(),
                playerName = doc.string("playerName") ?: "",
                hasJoinedBefore = doc.boolean("hasJoinedBefore"),
                firstJoin = doc.string("firstJoin"),
                pendingMessages = doc.list("pendingMessages").filterIsInstance<String>().toMutableList(),
                pendingPayments = doc.map<Double>("pendingPayments").toMutableMap(),
                playerSettings = doc.map<Boolean>("playerSettings").toMutableMap(),
                homes = doc.documents("homes").mapNotNull { Home.fromDocument(it) }.toMutableList(),
                economyLogs = doc.documents("economyLogs").mapNotNull { EconomyLog.fromDocument(it) }.toMutableList(),
                balance = doc.double("balance"),
                kitCooldowns = doc.map<Long>("kitCooldowns").toMutableMap(),
                lastJoin = doc.long("lastJoin").takeIf { it != 0L },
                totalPlaytime = doc.long("totalPlaytime"),
                playtimeHistory = doc.list("playtimeHistory").mapNotNull {
                    val parts = it.toString().split(":")
                    if (parts.size == 2) parts[0].toLongOrNull()?.let { a -> parts[1].toLongOrNull()?.let { b -> a to b } } else null
                }.toMutableList()

            )
        }

        fun create(uuid: UUID, name: String, collection: NitriteCollection): User {
            val user = User(
                uuidStr = uuid.toString(),
                playerName = name,
                hasJoinedBefore = false,
                firstJoin = null,
                pendingMessages = mutableListOf(),
                pendingPayments = mutableMapOf(),
                playerSettings = mutableMapOf(),
                homes = mutableListOf(),
                economyLogs = mutableListOf(),
                balance = 0.0,
                kitCooldowns = mutableMapOf(),
                lastJoin = null,
                totalPlaytime = 0L,
                playtimeHistory = mutableListOf()
            )

            collection.insert(user.toDocument())
            Logger.infoDB("Created new user $name ($uuid)")
            return user
        }
    }

    fun getEconomyLogs(page: Int, pageSize: Int = 10): CompletableFuture<List<String>> =
        CompletableFuture.supplyAsync {
            try {
                val start = (page - 1) * pageSize
                val end = minOf(start + pageSize, economyLogs.size)
                if (start >= economyLogs.size) return@supplyAsync emptyList()

                val formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm")
                economyLogs.subList(start, end).map { log ->
                    val timestamp = Instant.ofEpochMilli(log.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                        .let { "[$it]".gray() }

                    val amountStr = log.action.format(log.amount)
                    val reasonStr = if (log.reason.isNotBlank())
                        " &f${log.reason.translate()}" else " No reason found!"
                    "$amountStr$reasonStr $timestamp"
                }
            } catch (e: Exception) {
                Logger.warn("Failed to load economy logs for $playerName ($uuid): ${e.message}")
                emptyList()
            }
        }

    fun save(collection: NitriteCollection) {
        val filter = FluentFilter.where("uuidStr").eq(uuidStr)
        val result = collection.update(filter, toDocument())
        if (result.affectedCount == 0) {
            collection.insert(toDocument())
        }
        Logger.infoDB("Saved user $playerName ($uuidStr)")
    }

    fun isSettingEnabled(setting: Setting) = playerSettings.getOrDefault(setting.key, setting.defaultValue)
    fun setSetting(setting: Setting, value: Boolean) { playerSettings[setting.key] = value }
    fun toggleSetting(setting: Setting): Boolean = !isSettingEnabled(setting).also { setSetting(setting, it) }
    fun getAllSettings(): Map<Setting, Boolean> = Setting.values().associateWith { isSettingEnabled(it) }

    fun logEconomy(action: EcoAction, amount: Double, reason: String = "") {
        economyLogs.add(0, EconomyLog(action, amount, reason, System.currentTimeMillis()))
        if (economyLogs.size > 100) economyLogs.removeLast()
    }

    fun getFormattedFirstJoin() = firstJoin ?: "Never"

    fun queueMessage(msg: String){
        pendingMessages.add(msg)
        UserManager.save(this)
    }


    fun hasKitCooldown(kitId: String): Boolean {
        val expiry = kitCooldowns[kitId.lowercase()] ?: return false
        return System.currentTimeMillis() < expiry
    }

    fun getKitCooldownRemaining(kitId: String): Long {
        val expiry = kitCooldowns[kitId.lowercase()] ?: return 0
        return (expiry - System.currentTimeMillis()).coerceAtLeast(0)
    }

    fun setKitCooldown(kitId: String, durationMillis: Long) {
        kitCooldowns[kitId.lowercase()] = System.currentTimeMillis() + durationMillis
        UserManager.save(this)
    }


    fun queuePayment(senderName: String, amount: Double) {
        pendingPayments[senderName] = pendingPayments.getOrDefault(senderName, 0.0) + amount
        UserManager.save(this)
    }
}
