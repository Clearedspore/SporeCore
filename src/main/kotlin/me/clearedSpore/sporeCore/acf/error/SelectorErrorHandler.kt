package me.clearedSpore.sporeCore.acf.error

import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import org.bukkit.command.CommandSender

object SelectorErrorHandler {

    fun sendNoPlayerFound(sender: CommandSender, input: String) {
        sender.sendErrorMessage("That player '$input' has never joined the server before!")
    }

    fun sendNoEntityFound(sender: CommandSender, input: String) {
        sender.sendErrorMessage("No entity exists with the identifier '$input'.")
    }

    fun sendOnlyPlayersAllowed(sender: CommandSender, input: String) {
        sender.sendErrorMessage("This command only supports players, but '$input' is not a player.")
    }

    fun sendOnlyEntitiesAllowed(sender: CommandSender, input: String) {
        sender.sendErrorMessage("This command only supports non-player entities.")
    }

    fun sendNoPlayersInWorld(sender: CommandSender, world: String) {
        sender.sendErrorMessage("No players found in world '$world'.")
    }

    fun sendNoEntitiesInWorld(sender: CommandSender, world: String) {
        sender.sendErrorMessage("No entities found in world '$world'.")
    }

    fun sendNoPlayersInGamemode(sender: CommandSender, gamemode: String) {
        sender.sendErrorMessage("No players found in gamemode '$gamemode'.")
    }

    fun sendNoPlayersWithTag(sender: CommandSender, tag: String) {
        sender.sendErrorMessage("No players found with the tag '$tag'.")
    }

    fun sendNoPlayersWithPermission(sender: CommandSender, permission: String) {
        sender.sendErrorMessage("No players found with the permission '$permission'.")
    }

    fun sendNoPlayersInDistance(sender: CommandSender, distance: String) {
        sender.sendErrorMessage("No players found within distance '$distance'.")
    }

    fun sendNoEntitiesOfType(sender: CommandSender, type: String) {
        sender.sendErrorMessage("No entities of type '$type' found.")
    }

}
