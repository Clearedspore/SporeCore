package me.clearedSpore.sporeCore.commands.util

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean
import java.lang.reflect.Field
import java.text.DecimalFormat

@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@SporeCoreCommand
class UtilServerCommand : BaseCommand() {

    private val df = DecimalFormat("0.00")

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
        try {
            val server = Bukkit.getServer()
            val minecraftServerClass = server.javaClass.getMethod("getServer").declaringClass
            val tpsField: Field = minecraftServerClass.getDeclaredField("recentTps")
            tpsField.isAccessible = true
            val tps = tpsField.get(server) as DoubleArray

            sender.sendSuccessMessage(
                "Server TPS: " +
                        df.format(tps[0]).blue() +
                        " &7| " + "5min: ".white() + df.format(tps[1]).blue() +
                        " &7| " + "15min: ".white() + df.format(tps[2]).blue()
            )
        } catch (ex: Exception) {
            sender.sendErrorMessage("Failed to retrieve TPS")
        }
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
