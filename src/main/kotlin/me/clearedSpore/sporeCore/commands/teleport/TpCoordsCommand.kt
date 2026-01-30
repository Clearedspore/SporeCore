package me.clearedSpore.sporeCore.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

@CommandAlias("tpcoords|tpc|tppos")
@CommandPermission(Perm.TELEPORT_CORDS)
@SporeCoreCommand
class TpCoordsCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players|~ ~ ~ ~")
    fun onTpCoords(
        executor: Player,
        arg1: String,
        arg2: String,
        arg3: String,
        @Optional targetName: String?
    ) {
        val target: Player? = if (targetName != null) {
            Bukkit.getPlayer(targetName)
        } else {
            executor
        }

        if (target == null) {
            executor.sendErrorMessage("That player is not online!")
            return
        }


        val base = executor.location

        val x = parseCoordinate(base.x, arg1)?.let { String.format("%.1f", it).toDouble() }
        val y = parseCoordinate(base.y, arg2)?.let { String.format("%.1f", it).toDouble() }
        val z = parseCoordinate(base.z, arg3)?.let { String.format("%.1f", it).toDouble() }

        if (x == null || y == null || z == null) {
            executor.sendErrorMessage("Invalid coordinates.")
            return
        }


        target.teleport(Location(target.world, x, y, z))
        executor.sendSuccessMessage("Teleported ${target.name} to &f$x $y $z".blue())
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
