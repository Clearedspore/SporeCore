package me.clearedSpore.sporeCore.user

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.currency.`object`.CreditAction
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.util.Tasks
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object UserManager {
    private val users = mutableMapOf<UUID, User>()
    private val scheduler = Executors.newScheduledThreadPool(1)
    private val autoSaveTasks = mutableMapOf<UUID, ScheduledFuture<*>>()
    private val consoleUUID = UUID.nameUUIDFromBytes("Console".toByteArray())


    internal val userCollection get() = DatabaseManager.getUserCollection()

    fun get(uuid: UUID, name: String? = null): User? {
        users[uuid]?.let { return it }

        val loaded = User.load(uuid, userCollection)
        if (loaded != null) {
            val finalName = name ?: run {
                val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                if (offlinePlayer.hasPlayedBefore()) offlinePlayer.name ?: "Unknown"
                else "Unknown"
            }
            loaded.playerName = finalName
            users[uuid] = loaded
            return loaded
        }

        val finalName = name ?: run {
            val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
            if (offlinePlayer.hasPlayedBefore()) offlinePlayer.name ?: "Unknown"
            else "Unknown"
        }

        if (finalName.isNotBlank()) {
            val existingByName = userCollection.find().firstOrNull { doc ->
                doc.get("playerName", String::class.java)?.equals(finalName, ignoreCase = true) == true
            }

            if (existingByName != null) {
                val existingUuidStr = existingByName.get("uuidStr", String::class.java)
                if (!existingUuidStr.isNullOrBlank()) {
                    val existingUuid = UUID.fromString(existingUuidStr)
                    val existingUser = User.load(existingUuid, userCollection)
                    if (existingUser != null) {
                        existingUser.playerName = finalName
                        users[existingUuid] = existingUser
                        return existingUser
                    }
                }
            }
        }

        val alreadyExists = userCollection.find().any { it.get("uuidStr", String::class.java) == uuid.toString() }
        if (alreadyExists) {
            Logger.warn("UserManager: Tried to create new user for $uuid, but record already exists in DB. Skipping.")
            return User.load(uuid, userCollection)
        }

        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        if (!offlinePlayer.hasPlayedBefore()) return null

        val newUser = User.create(uuid, finalName, userCollection)
        users[uuid] = newUser
        return newUser
    }


    fun getConsoleUser(): User {
        return users[consoleUUID] ?: User(
            uuidStr = consoleUUID.toString(),
            playerName = "Console"
        ).also { users[consoleUUID] = it }
    }

    fun updateCache(user: User) {
        users[user.uuid] = user
    }

    fun getAllStoredUUIDsFromDB(): List<UUID> {
        return userCollection.find().mapNotNull {
            val id = it["uuidStr"] as? String ?: return@mapNotNull null
            runCatching { UUID.fromString(id) }.getOrNull()
        }.toList()
    }

    fun getAltsByLastIp(ip: String, excludeUuid: UUID? = null): List<User> {
        return getAllStoredUUIDsFromDB()
            .mapNotNull { get(it) }
            .filter { it.lastIp == ip && it.uuid != excludeUuid }
    }

    fun getAltsDeep(user: User): List<User> {
        val userIps = user.ipHistory.toSet()

        return getAllStoredUUIDsFromDB()
            .mapNotNull { get(it) }
            .filter { other ->
                other.uuid != user.uuid &&
                        other.ipHistory.any { it in userIps }
            }
    }


    fun get(player: Player): User? = get(player.uniqueId, player.name)
    fun get(player: OfflinePlayer): User? = get(player.uniqueId, player.name)

    fun getIfLoaded(uuid: UUID): User? = users[uuid]

    fun saveAllUsers(): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            users.values.forEach { save(it, true) }
        }

    fun save(user: User, silent: Boolean = false) {
        Tasks.runAsync {
            user.save(userCollection, silent)
        }
    }

    fun remove(uuid: UUID) {
        users.remove(uuid)
    }

    fun startAutoSave(user: User) {
        if (autoSaveTasks.containsKey(user.uuid)) return
        val task = scheduler.scheduleAtFixedRate({
            users[user.uuid]?.let { save(it) }
        }, 10, 10, TimeUnit.MINUTES)
        autoSaveTasks[user.uuid] = task
    }

    fun stopAutoSave(uuid: UUID) {
        autoSaveTasks.remove(uuid)?.cancel(false)
    }



    fun getBalance(uuid: UUID): CompletableFuture<Double?> =
        CompletableFuture.supplyAsync {
            get(uuid)?.balance
        }

    fun getCredits(uuid: UUID): CompletableFuture<Double?> =
        CompletableFuture.supplyAsync {
            get(uuid)?.credits
        }

    fun getTotalCreditsSpent(uuid: UUID): CompletableFuture<Double?> =
        CompletableFuture.supplyAsync {
            get(uuid)?.creditLogs
                ?.filter { it.action == CreditAction.SPENT }
                ?.sumOf { it.amount }
        }


    fun setBalance(uuid: UUID, amount: Double): CompletableFuture<Void> =
        CompletableFuture.runAsync {
            get(uuid)?.let {
                it.balance = amount
                it.save(userCollection)
            }
        }
}
