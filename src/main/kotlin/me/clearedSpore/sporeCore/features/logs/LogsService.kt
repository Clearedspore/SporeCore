package me.clearedSpore.sporeCore.features.logs

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.features.logs.`object`.Log
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.dizitart.no2.filters.Filter.and
import org.dizitart.no2.filters.FluentFilter.where
import org.dizitart.no2.index.IndexOptions
import org.dizitart.no2.index.IndexType
import java.util.*
import java.util.concurrent.TimeUnit

object LogsService {

    internal val logsCollection get() = DatabaseManager.getLogsCollection()
    private var cleanupTask: BukkitTask? = null

    fun initializeLogs() {
        if (!logsCollection.hasIndex("id")) {
            logsCollection.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), "id")
        }
        if (!logsCollection.hasIndex("sender")) {
            logsCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "sender")
        }
        if (!logsCollection.hasIndex("type")) {
            logsCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "type")
        }
        if (!logsCollection.hasIndex("timestamp")) {
            logsCollection.createIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE), "timestamp")
        }

        startCleanupTask()
    }

    fun startCleanupTask() {
        cleanupTask?.cancel()
        cleanupTask = Task.runRepeatedAsync(
            Runnable { cleanupLogs() },
            delay = 0,
            interval = 1,
            unit = TimeUnit.HOURS
        )
    }

    fun stopCleanupTask() {
        cleanupTask?.cancel()
        cleanupTask = null
    }

    fun cleanupLogs() {
        Logger.infoDB("Clearing logs...")
        val config = SporeCore.instance.coreConfig.logs
        val maxAge = TimeUtil.parseDuration(config.cleanupTime)
        val now = System.currentTimeMillis()

        val toRemove = logsCollection.find().map { Log.fromDocument(it) }.filter { now - it.timestamp >= maxAge }

        if (toRemove.isEmpty()) return

        Logger.infoDB("Removing ${toRemove.size} logs")

        toRemove.forEach {
            logsCollection.remove(where("id").eq(it.id))
        }

        Logger.infoDB("Removed ${toRemove.size} logs")
    }




    fun addLog(senderUuid: String, action: String, type: LogType) {
        val log = Log(
            UUID.randomUUID().toString(),
            senderUuid,
            type,
            action,
            System.currentTimeMillis()
        )
        logsCollection.insert(log.toDocument())
    }


    fun addLog(sender: Player, action: String, type: LogType) {
        addLog(sender.uniqueId.toString(), action, type)
    }

    fun addLog(sender: CommandSender, action: String, type: LogType) {
        val uuid = if (sender is Player) sender.uniqueId else UserManager.getConsoleUser().uuid
        addLog(uuid.toString(), action, type)
    }

    fun addLog(log: Log) {
        logsCollection.insert(log.toDocument())
    }

    fun deleteLog(id: String) {
        logsCollection.remove(where("id").eq(id))
    }

    fun clearLogsByType(type: LogType) {
        logsCollection.remove(where("type").eq(type.name))
    }

    fun findLog(id: String): Log? =
        logsCollection.find(where("id").eq(id)).firstOrNull()?.let { Log.fromDocument(it) }


    fun findLogsBySender(senderUuid: String): List<Log> =
        logsCollection.find(
            where("sender").eq(senderUuid)
        ).map { Log.fromDocument(it) }.toList()

    fun findLogs(type: LogType, senderUuid: String): List<Log> =
        logsCollection.find(
            where("type").eq(type.name)
                .and(where("sender").eq(senderUuid))
        ).map { Log.fromDocument(it) }.toList()

    fun findLogsBySenderAndTime(senderUuid: String, from: Long, to: Long): List<Log> =
        logsCollection.find(
            where("sender").eq(senderUuid)
                .and(where("timestamp").gte(from).and(where("timestamp").lte(to)))
        ).map { Log.fromDocument(it) }.toList()


    fun findLogsByType(type: LogType): List<Log> =
        logsCollection.find(where("type").eq(type.name)).map { Log.fromDocument(it) }.toList()

    fun findLogsBySenderAndActionContains(senderUuid: String, actionPart: String): List<Log> =
        logsCollection.find(
            and(
                where("sender").eq(senderUuid),
                where("action").regex(".*${Regex.escape(actionPart)}.*")
            )
        ).map { Log.fromDocument(it) }.toList()
}
