package me.clearedSpore.sporeCore.commands.util

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.button.TextButton
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@SporeCoreCommand
class UtilWorldCommand : BaseCommand() {

    @Subcommand("world name")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@worlds")
    fun worldName(sender: CommandSender, @Optional @Name("world") worldName: String? = null) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        sender.sendSuccessMessage("World name: &f${world.name}")
        val button = TextButton("[Copy]".blue())
            .copyToClipboard(world.name)
            .hoverEvent("Click to copy!")
            .onClick {
                sender.sendSuccessMessage("Successfully copied!")
            }
            .build(sender)
        if (sender is Player) {
            sender.sendMessage(button)
        }

    }

    @Subcommand("world getspawn")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@worlds")
    fun getSpawn(sender: CommandSender, @Optional @Name("world") worldName: String? = null) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        val loc = world.spawnLocation
        sender.sendSuccessMessage("Spawn of ${world.name}: x=${loc.blockX} y=${loc.blockY} z=${loc.blockZ}")
        val copyString = "${loc.blockX}, ${loc.blockY}, ${loc.blockZ}, ${loc.world?.name ?: "world"}"
        val button = TextButton("[Copy]".blue())
            .copyToClipboard(copyString)
            .hoverEvent("Click to copy!")
            .onClick {
                sender.sendSuccessMessage("Successfully copied!")
            }
            .build(sender)
        if (sender is Player) {
            sender.sendMessage(button)
        }
    }

    @Subcommand("world setspawn")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@nothing @nothing @nothing @worlds")
    fun setSpawn(
        sender: CommandSender,
        @Name("x") x: Int,
        @Name("y") y: Int,
        @Name("z") z: Int,
        @Optional @Name("world") worldName: String? = null
    ) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        world.spawnLocation = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
        sender.sendSuccessMessage("Set spawn of ${world.name} to x=$x y=$y z=$z")
    }

    @Subcommand("world weather")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@worlds")
    fun setWeather(
        sender: CommandSender,
        @Name("type") weatherType: String,
        @Optional @Name("world") worldName: String? = null
    ) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        when (weatherType.lowercase()) {
            "clear" -> world.setStorm(false)
            "rain" -> world.setStorm(true)
            "thunder" -> {
                world.isThundering = true; world.setStorm(true)
            }

            else -> {
                sender.sendErrorMessage("Invalid weather type! Use clear, rain, or thunder.")
                return
            }
        }
        sender.sendSuccessMessage("Set weather in ${world.name} to $weatherType")
    }

    @Subcommand("world time")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@nothing @worlds")
    fun setTime(sender: CommandSender, @Name("time") time: Long, @Optional @Name("world") worldName: String? = null) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        world.time = time
        sender.sendSuccessMessage("Set time in ${world.name} to $time")
    }

    @Subcommand("world day")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@worlds")
    fun setDay(sender: CommandSender, @Optional @Name("world") worldName: String? = null) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        world.time = 1000
        sender.sendSuccessMessage("Set ${world.name} to day")
    }

    @Subcommand("world night")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@worlds")
    fun setNight(sender: CommandSender, @Optional @Name("world") worldName: String? = null) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        world.time = 13000
        sender.sendSuccessMessage("Set ${world.name} to night")
    }

    @Subcommand("world list")
    @CommandPermission(Perm.UTIL_WORLD)
    fun listWorlds(sender: CommandSender) {
        val worlds = Bukkit.getWorlds()

        sender.sendSuccessMessage("Loaded worlds:")
        worlds.forEach { world ->
            sender.sendMessage("- ".blue() + " ${world.name}".white())
        }

    }

    @Subcommand("world info")
    @CommandPermission(Perm.UTIL_WORLD)
    @CommandCompletion("@worlds")
    fun worldInfo(sender: CommandSender, @Optional @Name("world") worldName: String? = null) {
        val world = if (worldName != null) Bukkit.getWorld(worldName) else (sender as? Player)?.world

        if (world == null) {
            sender.sendErrorMessage("World not found!")
            return
        }

        sender.sendSuccessMessage(
            "World info for ${world.name}: \nSeed=${world.seed} \nEnvironment=${world.environment} \nPlayers=${world.players.size}"
        )
    }

}
