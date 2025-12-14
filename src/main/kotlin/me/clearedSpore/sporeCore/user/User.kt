package me.clearedSpore.sporeCore.user

import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.database.util.DocReader
import me.clearedSpore.sporeCore.database.util.DocWriter
import me.clearedSpore.sporeCore.features.chat.color.`object`.ChatColor
import me.clearedSpore.sporeCore.features.chat.`object`.ChatFormat
import me.clearedSpore.sporeCore.features.currency.`object`.CreditAction
import me.clearedSpore.sporeCore.features.currency.`object`.CreditLog
import me.clearedSpore.sporeCore.features.eco.`object`.EcoAction
import me.clearedSpore.sporeCore.features.eco.`object`.EconomyLog
import me.clearedSpore.sporeCore.features.homes.`object`.Home
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.punishment.`object`.StaffPunishmentStats
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.user.settings.Setting
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.dizitart.no2.collection.Document
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
    var firstServerIP: String? = null,
    var balance: Double = 0.0,
    var pendingMessages: MutableList<String> = mutableListOf(),
    var pendingPayments: MutableMap<String, Double> = mutableMapOf(),
    var playerSettings: MutableMap<String, Boolean> = mutableMapOf(),
    var homes: MutableList<Home> = mutableListOf(),
    var economyLogs: MutableList<EconomyLog> = mutableListOf(),
    var kitCooldowns: MutableMap<String, Long> = mutableMapOf(),
    var lastJoin: String? = null,
    var totalPlaytime: Long = 0L,
    var playtimeHistory: MutableList<Pair<Long, Long>> = mutableListOf(),
    var credits: Double = 0.0,
    var creditLogs: MutableList<CreditLog> = mutableListOf(),
    var creditsSpent: MutableList<CreditLog> = mutableListOf(),
    var lastLocation: Location? = null,
    var chatColor: ChatColor? = null,
    var chatFormat: ChatFormat? = null,
    var punishments: MutableList<Punishment> = mutableListOf(),
    var lastIp: String? = null,
    var ipHistory: MutableList<String> = mutableListOf(),
    var channel: String? = null,
    var staffStats: MutableList<StaffPunishmentStats> = mutableListOf(),
    var lastServerIP: String? = null,
    var discordID: String? = null,
    var pendingInventories: MutableSet<String> = mutableSetOf(),
    var tpsBar: Boolean = false
) {
    val uuid: UUID get() = UUID.fromString(uuidStr)
    val player: Player? get() = Bukkit.getPlayer(uuid)
    val isConsole: Boolean
        get() = uuidStr.equals("CONSOLE", ignoreCase = true) || playerName.equals("CONSOLE", ignoreCase = true)

    fun isOnline(): Boolean {
        return player?.isOnline == true
    }

    fun toDocument() = DocWriter()
        .put("uuidStr", uuidStr)
        .put("playerName", playerName)
        .put("hasJoinedBefore", hasJoinedBefore)
        .put("firstJoin", firstJoin)
        .put("firstServerIP", firstServerIP)
        .putList("pendingMessages", pendingMessages)
        .putMap("pendingPayments", pendingPayments)
        .putMap("playerSettings", playerSettings)
        .putDocuments("homes", homes.map { it.toDocument() })
        .putDocuments("economyLogs", economyLogs.map { it.toDocument() })
        .putDouble("balance", balance)
        .putMap("kitCooldowns", kitCooldowns.mapValues { it.value as Long })
        .putString("lastJoin", lastJoin ?: "Never")
        .putLong("totalPlaytime", totalPlaytime)
        .putList("playtimeHistory", playtimeHistory.map { "${it.first}:${it.second}" })
        .putDouble("credits", credits)
        .putDocuments("creditLogs", creditLogs.map { it.toDocument() })
        .putDocuments("creditsSpent", creditsSpent.map { it.toDocument() })
        .putLocation("lastLocation", lastLocation)
        .put("chatColor", chatColor?.toDocument())
        .put("chatFormat", chatFormat?.toDocument())
        .putDocuments("punishments", punishments.map { it.toDocument() })
        .putString("lastIp", lastIp ?: "")
        .putList("ipHistory", ipHistory)
        .put("channel", channel)
        .putDocuments("staffStats", staffStats.map { it.toDocument() })
        .put("lastServerIP", lastServerIP)
        .put("discordID", discordID)
        .putList("pendingInventories", pendingInventories.toList())
        .putBoolean("tpsBar", tpsBar)
        .build()

    fun ChatColor.toDocument(): Document = DocWriter()
        .putString("name", name)
        .putString("colorString", colorString)
        .build()

    fun ChatFormat.toDocument(): Document = DocWriter()
        .putBoolean("bold", bold)
        .putBoolean("italic", italic)
        .putBoolean("underline", underline)
        .putBoolean("striketrough", striketrough)
        .putBoolean("magic", magic)
        .putBoolean("none", none)
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
                firstServerIP = doc.string("firstServerIP"),
                pendingMessages = doc.list("pendingMessages").filterIsInstance<String>().toMutableList(),
                pendingPayments = doc.map<Double>("pendingPayments").toMutableMap(),
                playerSettings = doc.map<Boolean>("playerSettings").toMutableMap(),
                homes = doc.documents("homes").mapNotNull { Home.fromDocument(it) }.toMutableList(),
                economyLogs = doc.documents("economyLogs").mapNotNull { EconomyLog.fromDocument(it) }.toMutableList(),
                balance = doc.double("balance"),
                kitCooldowns = (doc.doc.get("kitCooldowns") as? Map<*, *>)?.mapNotNull { (k, v) ->
                    val key = k?.toString() ?: return@mapNotNull null
                    val value = when (v) {
                        is Number -> v.toLong()
                        is String -> v.toLongOrNull()
                        else -> null
                    } ?: return@mapNotNull null
                    key to value
                }?.toMap()?.toMutableMap() ?: mutableMapOf(),
                lastJoin = doc.string("lastJoin"),
                totalPlaytime = doc.long("totalPlaytime"),
                playtimeHistory = doc.list("playtimeHistory").mapNotNull {
                    val parts = it.toString().split(":")
                    if (parts.size == 2) parts[0].toLongOrNull()
                        ?.let { a -> parts[1].toLongOrNull()?.let { b -> a to b } } else null
                }.toMutableList(),
                credits = doc.double("credits"),
                creditLogs = doc.documents("creditLogs").mapNotNull { CreditLog.fromDocument(it) }.toMutableList(),
                creditsSpent = doc.documents("creditsSpent").mapNotNull { CreditLog.fromDocument(it) }.toMutableList(),
                lastLocation = doc.location("lastLocation"),
                chatColor = ChatColor.fromDocument(doc.document("chatColor")),
                chatFormat = ChatFormat.fromDocument(doc.document("chatFormat")),
                punishments = doc.documents("punishments").mapNotNull { punDoc ->
                    Punishment.fromDocument(punDoc)
                }.toMutableList(),
                lastIp = doc.string("lastIp"),
                ipHistory = doc.list("ipHistory").filterIsInstance<String>().toMutableList(),
                channel = doc.string("channel"),
                staffStats = doc.documents("staffStats").mapNotNull { StaffPunishmentStats.fromDocument(it) }.toMutableList(),
                lastServerIP = doc.string("lastServerIP"),
                discordID = doc.string("discordID"),
                pendingInventories = doc.list("pendingInventories")
                    .mapNotNull { it?.toString() }
                    .toMutableSet(),
                tpsBar = doc.boolean("tpsBar")
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
                playtimeHistory = mutableListOf(),
                credits = 0.0,
                creditLogs = mutableListOf(),
                creditsSpent = mutableListOf(),
                lastLocation = null,
                chatColor = null,
                chatFormat = null,
                punishments = mutableListOf(),
                channel = null,
                staffStats = mutableListOf(),
                discordID = null,
                tpsBar = false,
                pendingInventories = mutableSetOf()
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

    fun getCreditLogs(page: Int, pageSize: Int = 10): CompletableFuture<List<String>> =
        CompletableFuture.supplyAsync {
            try {
                val start = (page - 1) * pageSize
                val end = minOf(start + pageSize, creditLogs.size)
                if (start >= creditLogs.size) return@supplyAsync emptyList()

                val formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm")
                creditLogs.subList(start, end).map { log ->
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
                Logger.warn("Failed to load credit logs for $playerName ($uuid): ${e.message}")
                emptyList()
            }
        }

    fun sendMessage(message: String) {
        val formatted = message.translate()

        when {
            isConsole -> {
                Bukkit.getConsoleSender().sendMessage(formatted)
            }

            player != null -> {
                player!!.sendMessage(formatted)
            }

            else -> {
                pendingMessages.add(message)
                UserManager.save(this, silent = true)
            }
        }
    }


    fun save(collection: NitriteCollection, silent: Boolean = false) {
        val filter = FluentFilter.where("uuidStr").eq(uuidStr)
        val result = collection.update(filter, toDocument())
        if (result.affectedCount == 0) {
            collection.insert(toDocument())
        }
        if (!silent) {
            Logger.infoDB("Saved user $playerName ($uuidStr)")
        }
    }

    fun setSetting(setting: Setting, value: Boolean) {
        playerSettings[setting.key] = value
    }

    fun isSettingEnabled(setting: Setting): Boolean {
        return playerSettings[setting.key] ?: setting.defaultValue
    }

    fun toggleSetting(setting: Setting): Boolean {
        val newValue = !isSettingEnabled(setting)
        setSetting(setting, newValue)
        UserManager.save(this)
        return newValue
    }

    fun getAllSettings(): Map<Setting, Boolean> = Setting.values().associateWith { isSettingEnabled(it) }

    fun logEconomy(action: EcoAction, amount: Double, reason: String = "") {
        economyLogs.add(0, EconomyLog(action, amount, reason, System.currentTimeMillis()))
        if (economyLogs.size > 100) economyLogs.removeLast()
    }


    fun kick(message: String? = null): Boolean {
        val player = this.player ?: return false
        return try {
            if (message.isNullOrBlank()) {
                player.kickPlayer(null)
            } else {
                player.kickPlayer(message.translate())
            }
            true
        } catch (e: Exception) {
            Logger.warn("Failed to kick ${player.name}: ${e.message}")
            false
        }
    }


    fun logCredit(action: CreditAction, amount: Double, reason: String = "") {
        creditLogs.add(0, CreditLog(action, amount, reason, System.currentTimeMillis()))
        if (creditLogs.size > 100) creditLogs.removeLast()
    }


    fun isBanned(): Boolean = getActivePunishment(PunishmentType.BAN) != null

    fun isMuted(): Boolean = getActivePunishment(PunishmentType.MUTE) != null

    fun getActivePunishments(): List<Punishment> {
        val now = Date()
        return punishments.filter { it.removalDate == null && (it.expireDate == null || it.expireDate!!.after(now) && it.type != PunishmentType.KICK) }
    }


    fun getActivePunishment(type: PunishmentType): Punishment? {
        val now = Date()

        val relatedTypes = when (type) {
            PunishmentType.BAN, PunishmentType.TEMPBAN ->
                listOf(PunishmentType.BAN, PunishmentType.TEMPBAN)

            PunishmentType.MUTE, PunishmentType.TEMPMUTE ->
                listOf(PunishmentType.MUTE, PunishmentType.TEMPMUTE)

            PunishmentType.WARN, PunishmentType.TEMPWARN ->
                listOf(PunishmentType.WARN, PunishmentType.TEMPWARN)

            PunishmentType.KICK ->
                listOf(PunishmentType.KICK)

            else ->
                listOf(type)
        }

        return punishments
            .filter { it.type in relatedTypes && it.removalDate == null }
            .firstOrNull { it.expireDate == null || it.expireDate!!.after(now) }
    }


    fun getPunishmentsByType(type: PunishmentType): List<Punishment> {
        val relatedTypes = when (type) {
            PunishmentType.BAN, PunishmentType.TEMPBAN -> listOf(PunishmentType.BAN, PunishmentType.TEMPBAN)
            PunishmentType.MUTE, PunishmentType.TEMPMUTE -> listOf(PunishmentType.MUTE, PunishmentType.TEMPMUTE)
            PunishmentType.WARN, PunishmentType.TEMPWARN -> listOf(PunishmentType.WARN, PunishmentType.TEMPWARN)
            else -> listOf(type)
        }

        return punishments.filter { it.type in relatedTypes }
    }





    fun getLastPunishment(type: PunishmentType? = null): Punishment? {
        val filtered = if (type != null) {
            val relatedTypes = when (type) {
                PunishmentType.BAN, PunishmentType.TEMPBAN -> listOf(PunishmentType.BAN, PunishmentType.TEMPBAN)
                PunishmentType.MUTE, PunishmentType.TEMPMUTE -> listOf(PunishmentType.MUTE, PunishmentType.TEMPMUTE)
                PunishmentType.WARN, PunishmentType.TEMPWARN -> listOf(PunishmentType.WARN, PunishmentType.TEMPWARN)
                else -> listOf(type)
            }
            punishments.filter { it.type in relatedTypes }
        } else {
            punishments
        }

        return filtered.maxByOrNull { punishment: Punishment -> punishment.punishDate.time }
    }


    fun unban(sender: User, punishmentId: String, reason: String): Boolean {
        return PunishmentService.removePunishment(this, sender, punishmentId, reason)
    }

    fun unmute(sender: User, punishmentId: String, reason: String): Boolean {
        return PunishmentService.removePunishment(this, sender, punishmentId, reason)
    }

    fun unwarn(sender: User, punishmentId: String, reason: String): Boolean {
        return PunishmentService.removePunishment(this, sender, punishmentId, reason)
    }

    fun queueMessage(msg: String) {
        pendingMessages.add(msg)
        UserManager.save(this)
    }


    fun hasKitCooldown(kitID: String): Boolean {
        val expiry = kitCooldowns[kitID] ?: return false
        return System.currentTimeMillis() < expiry
    }

    fun getKitCooldownRemaining(kitID: String): Long {
        val expiry = kitCooldowns[kitID] ?: return 0L
        return maxOf(expiry - System.currentTimeMillis(), 0L)
    }

    fun setKitCooldown(kitID: String, durationMillis: Long) {
        kitCooldowns[kitID] = System.currentTimeMillis() + durationMillis
        UserManager.save(this)
    }

    fun queuePayment(senderName: String, amount: Double) {
        pendingPayments[senderName] = pendingPayments.getOrDefault(senderName, 0.0) + amount
        UserManager.save(this)
    }
}
