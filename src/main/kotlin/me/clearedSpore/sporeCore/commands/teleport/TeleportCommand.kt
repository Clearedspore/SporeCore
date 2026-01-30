package me.clearedSpore.sporeCore.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("tp|teleport|goto")
@CommandPermission(Perm.TELEPORT)
@SporeCoreCommand()
class TeleportCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets|~ @targets|~ ~ ~")
    fun onTp(
        sender: CommandSender,
        arg1: String,
        @Optional arg2: String?,
        @Optional arg3: String?,
        @Optional arg4: String?,
        @Optional targets: TargetPlayers?
    ) {
        val playerSender = sender as? Player

        val firstTargets = targets ?: run {
            Bukkit.getPlayerExact(arg1)?.let { listOf(it) } ?: emptyList()
        }

        val shouldLog = SporeCore.instance.coreConfig.logs.teleports

        when {
            arg2 == null && firstTargets.isNotEmpty() -> {
                if (playerSender == null) {
                    sender.sendErrorMessage("Console must specify a target player.")
                    return
                }
                val target = firstTargets.first()
                playerSender.teleport(target.location)
                Logger.log(sender, Perm.LOG, "teleported to ${target.name}", false)
                sender.sendMessage("Teleported to ${target.name}.".blue())
                if (shouldLog) {
                    LogsService.addLog(sender.uuidStr(), "Teleported to ${target.name}", LogType.TELEPORT)
                }
            }

            arg2 != null && arg3 == null -> {
                val secondTarget = Bukkit.getPlayerExact(arg2)
                if (secondTarget == null) {
                    sender.sendErrorMessage("The target player '${arg2}' is not online.")
                    return
                }

                if (!sender.hasPermission(Perm.TELEPORT_OTHERS) && playerSender !in firstTargets) {
                    sender.sendErrorMessage("You don't have permission to teleport others!")
                    return
                }

                firstTargets.forEach { it.teleport(secondTarget.location) }
                firstTargets.forEach {
                    Logger.log(sender, Perm.LOG, "teleported ${it.name} to ${secondTarget.name}", false)
                    sender.sendMessage("Teleported ${it.name} to ${secondTarget.name}.".blue())
                    if (shouldLog) {
                        LogsService.addLog(
                            sender.uuidStr(),
                            "Teleported ${it.name} to ${secondTarget.name}",
                            LogType.TELEPORT
                        )
                    }
                }
            }

            arg2 != null && arg3 != null && arg4 == null -> {
                if (playerSender == null) {
                    sender.sendErrorMessage("Console must specify a player for coordinate teleport.")
                    return
                }
                if (!sender.hasPermission(Perm.TELEPORT_CORDS)) {
                    sender.sendErrorMessage("You don't have permission to use coordinate teleport.")
                    return
                }

                val loc = parseCoordinates(playerSender.location, arg1, arg2, arg3)
                if (loc == null) {
                    sender.sendErrorMessage("Invalid coordinates.")
                    return
                }

                playerSender.teleport(loc)
                Logger.log(
                    sender,
                    Perm.LOG,
                    "teleported ${playerSender.name} to coordinates ${loc.x} ${loc.y} ${loc.z}",
                    false
                )
                sender.sendMessage(
                    "Teleported to &f${formatCoord(loc.x)} ${formatCoord(loc.y)} ${formatCoord(loc.z)}".blue()
                )
                if (shouldLog) {
                    LogsService.addLog(
                        sender.uuidStr(),
                        "Teleported to ${playerSender.name} to coordinates ${loc.x} ${loc.y} ${loc.z}",
                        LogType.TELEPORT
                    )
                }

            }

            arg2 != null && arg3 != null && arg4 != null -> {
                if (!sender.hasPermission(Perm.TELEPORT_CORDS)) {
                    sender.sendErrorMessage("You don't have permission to teleport players to coordinates!")
                    return
                }
                if (firstTargets.isEmpty() && playerSender == null) {
                    sender.sendErrorMessage("No valid players to teleport.")
                    return
                }

                if (firstTargets.isEmpty()) {
                    sender.sendErrorMessage("That player is not online.")
                    return
                }

                val targetsToTeleport = firstTargets

                val loc = parseCoordinates(targetsToTeleport.first().location, arg2, arg3, arg4)
                if (loc == null) {
                    sender.sendErrorMessage("Invalid coordinates.")
                    return
                }

                targetsToTeleport.forEach { target ->
                    target.teleport(loc)
                    Logger.log(
                        sender,
                        Perm.LOG,
                        "teleported ${target.name} to coordinates ${loc.x} ${loc.y} ${loc.z}",
                        false
                    )
                    sender.sendMessage(
                        "Teleported ${target.name} to &f${formatCoord(loc.x)} ${formatCoord(loc.y)} ${formatCoord(loc.z)}".blue()
                    )
                    if (shouldLog) {
                        LogsService.addLog(
                            sender.uuidStr(),
                            "Teleported ${target.name} to ${formatCoord(loc.x)} ${formatCoord(loc.y)} ${formatCoord(loc.z)}",
                            LogType.TELEPORT
                        )
                    }
                }
            }

            else -> sender.sendErrorMessage(
                "Invalid usage: /tp <player> | /tp <player1> <player2> | /tp <x> <y> <z> | /tp <player(s)> <x> <y> <z>"
            )
        }
    }

    private fun formatCoord(value: Double): String {
        return String.format("%.1f", value)
    }


    private fun parseCoordinates(base: Location, xArg: String, yArg: String, zArg: String): Location? {
        val world = base.world ?: return null
        val x = parseCoordinate(base.x, xArg) ?: return null
        val y = parseCoordinate(base.y, yArg) ?: return null
        val z = parseCoordinate(base.z, zArg) ?: return null
        return Location(world, x, y, z)
    }

    private fun parseCoordinate(base: Double, input: String): Double? {
        return try {
            if (input.startsWith("~")) {
                if (input == "~") base else base + input.substring(1).toDouble()
            } else input.toDouble()
        } catch (e: Exception) {
            null
        }
    }
}
