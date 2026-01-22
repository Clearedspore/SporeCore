package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.annotation.Optional
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeAPI.util.TimeUtil.TimeUnitStyle
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.button.TextButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.*

@CommandAlias("getlogs")
@CommandPermission(Perm.VIEW_LOGS)
class GetLogsCommand : BaseCommand() {

    private val pageSize = 6

    @Default
    @CommandCompletion("@players @logtypes @range:1-20 time=|contains=")
    @Syntax("<player> <type> [page] [time=<time>] [contains=<text>]")
    fun onLogs(
        sender: CommandSender,
        targetName: String,
        typeArg: String,
        @Optional pageArg: Int?,
        vararg extraArgs: String
    ) {
        val targetPlayer = Bukkit.getOfflinePlayer(targetName)
        if (!targetPlayer.hasPlayedBefore()) {
            sender.sendMessage("Player not found.".red())
            return
        }

        val targetUUID = targetPlayer.uniqueId ?: run {
            sender.sendMessage("Could not retrieve player UUID.".red())
            return
        }

        val type = try {
            LogType.valueOf(typeArg.uppercase())
        } catch (_: Exception) {
            sender.sendMessage("Invalid log type.".red())
            return
        }

        var page = pageArg ?: 1
        var timeFilter: Long? = null
        var containsFilter: String? = null

        extraArgs.forEach { arg ->
            when {
                arg.startsWith("time=", true) -> {
                    val durationStr = arg.substringAfter("=")
                    val durationMs = TimeUtil.parseDuration(durationStr)
                    timeFilter = System.currentTimeMillis() - durationMs
                }

                arg.startsWith("contains=", true) -> containsFilter = arg.substringAfter("=")
            }
        }

        sender.sendMessage("Fetching logs async...".blue())

        Task.runAsync {
            var logs = LogsService.findLogs(type, targetUUID.toString())

            timeFilter?.let { fromTime -> logs = logs.filter { it.timestamp >= fromTime } }
            containsFilter?.let { contains -> logs = logs.filter { it.action.contains(contains, ignoreCase = true) } }

            logs = logs.sortedByDescending { it.timestamp }

            if (logs.isEmpty()) {
                sender.sendMessage("No logs found.".red())
                return@runAsync
            }

            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, logs.size)
            if (startIndex >= logs.size) {
                sender.sendMessage("No logs on this page.".red())
                return@runAsync
            }

            sender.sendMessage("----------------------------------".gray())
            val timeText = timeFilter?.let {
                " after ${
                    TimeUtil.formatDuration(
                        System.currentTimeMillis() - it,
                        TimeUnitStyle.SHORT,
                        2
                    )
                }"
            } ?: ""
            sender.sendMessage("Showing ${type.name.lowercase()} logs for ${targetPlayer.name}$timeText".blue())
            sender.sendMessage("")

            logs.subList(startIndex, endIndex).forEach { log ->
                val durationAgo = TimeUtil.formatDuration(System.currentTimeMillis() - log.timestamp)
                val hoverTime = Date(log.timestamp).toString()
                val senderName = try {
                    val uuid = UUID.fromString(log.sender)
                    Bukkit.getOfflinePlayer(uuid).name ?: "Console"
                } catch (_: Exception) {
                    "Console"
                }

                val msg = TextButton("[${durationAgo} ago]".gray() + " ${senderName.blue()} -> ${log.action.white()}")
                    .hoverEvent(hoverTime)
                    .build(sender)
                sender.sendMessage(msg)
            }

            sender.sendMessage("")

            val isLastPage = endIndex >= logs.size
            val nextPage = page + 1
            val baseCmd = mutableListOf("/getlogs", targetName, type.name, nextPage.toString())
            extraArgs.forEach { arg ->
                if (!arg.startsWith("time=", true) && !arg.startsWith("contains=", true)) return@forEach
                baseCmd.add(arg)
            }
            val nextPageCmd = baseCmd.joinToString(" ")

            val nextPageText = if (isLastPage) {
                Component.text("This was the last page. There are no more logs.", NamedTextColor.GRAY)
            } else {
                TextButton("Run to view page $nextPage")
                    .runCommand(nextPageCmd)
                    .hoverEvent(nextPageCmd.gray())
                    .build(sender)
            }

            sender.sendMessage(nextPageText)
            sender.sendMessage("----------------------------------".gray())
        }
    }
}
