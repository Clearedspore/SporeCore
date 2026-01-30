package me.clearedSpore.sporeCore.commands.gamemode

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("gamemode|gm")
@CommandPermission(Perm.GAMEMODE)
@SporeCoreCommand
class GamemodeCommand : BaseCommand() {

    @Default
    @Syntax("<gamemode> <player>")
    @CommandCompletion("@gamemodes @targets")
    fun onGamemode(sender: CommandSender, gamemode: String, @Optional targets: TargetPlayers?) {

        val mode = try {
            GameMode.valueOf(gamemode.uppercase())
        } catch (e: IllegalArgumentException) {
            sender.sendMessage("Invalid gamemode! Use creative, survival, spectator, or adventure.".red())
            return
        }

        val permission = when (mode) {
            GameMode.CREATIVE -> Perm.CREATIVE
            GameMode.SURVIVAL -> Perm.SURVIVAL
            GameMode.SPECTATOR -> Perm.SPECTATOR
            GameMode.ADVENTURE -> Perm.ADVENTURE
        }

        if (!sender.hasPermission(permission)) {
            sender.sendMessage(
                "You do not have permission to switch to ${mode.name.lowercase().capitalizeFirstLetter()}!".red()
            )
            return
        }

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a player when running this command from console.")
        }

        val players = resolved.filter { sender == it || sender.hasPermission(Perm.GAMEMODE_OTHERS) }

        if (players.isEmpty()) {
            throw InvalidCommandArgument("No valid players.")
        }

        players.forEach { target ->
            target.gameMode = mode
            if (sender == target) {
                Logger.log(sender, Perm.LOG, "changed their gamemode to ${mode.name.capitalizeFirstLetter()}", false)
            } else {
                Logger.log(
                    sender,
                    Perm.LOG,
                    "changed ${target.name}’s gamemode to ${mode.name.capitalizeFirstLetter()}",
                    false
                )
            }
            sender.sendMessage("You updated ".blue() + target.name.white() + "’s gamemode to ${mode.name.capitalizeFirstLetter()}.".blue())
        }
    }
}
