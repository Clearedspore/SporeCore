package me.clearedSpore.sporeCore.commands.util

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean
import java.text.DecimalFormat
import kotlin.math.min

@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@RegisterCommand
class UtilServerCommand : SporeCommand() {

    @Subcommand("server players")
    @CommandPermission(Perm.UTIL_SERVER)
    fun players(sender: CommandSender) {
        val online = Bukkit.getOnlinePlayers().size
        val max = Bukkit.getMaxPlayers()
        sender.sendSuccessMessage("Online players: &f$online&7/&f$max")
    }

    @Subcommand("server memory")
    @CommandPermission(Perm.UTIL_SERVER)
    fun memory(sender: CommandSender) {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val max = runtime.maxMemory() / 1024 / 1024
        val free = runtime.freeMemory() / 1024 / 1024
        sender.sendSuccessMessage(
            "Memory usage: " +
                    "Used: ".white() + "${used}MB".green() +
                    " &7| " + "Free: ".white() + "${free}MB".green() +
                    " &7| " + "Max: ".white() + "${max}MB".green()
        )

    }

    @Subcommand("server tps")
    @CommandPermission(Perm.UTIL_SERVER)
    fun tps(sender: CommandSender) {

        val tps = try {
            round1(min(20.0, SporeCore.instance.server.tps[0]))
        } catch (ex: Exception) {
            20.0
        }

        sender.sendSuccessMessage(
            "Server TPS: $tps"
        )
    }

    private fun round1(value: Double): Double {
        return kotlin.math.round(value * 10.0) / 10.0
    }

    @Subcommand("server version")
    @CommandPermission(Perm.UTIL_SERVER)
    fun version(sender: CommandSender) {
        sender.sendSuccessMessage(
            "Server version: ".white() + Bukkit.getVersion().blue() +
                    " &7| " +
                    "Bukkit version: ".white() + Bukkit.getBukkitVersion().blue()
        )

    }

    @Subcommand("server uptime")
    @CommandPermission(Perm.UTIL_SERVER)
    fun uptime(sender: CommandSender) {
        val runtime: RuntimeMXBean = ManagementFactory.getRuntimeMXBean()
        val uptimeMillis = runtime.uptime
        val uptimeSeconds = uptimeMillis / 1000 % 60
        val uptimeMinutes = uptimeMillis / (1000 * 60) % 60
        val uptimeHours = uptimeMillis / (1000 * 60 * 60) % 24
        val uptimeDays = uptimeMillis / (1000 * 60 * 60 * 24)
        sender.sendSuccessMessage("Server uptime: &f${uptimeDays}d ${uptimeHours}h ${uptimeMinutes}m ${uptimeSeconds}s")
    }

}
