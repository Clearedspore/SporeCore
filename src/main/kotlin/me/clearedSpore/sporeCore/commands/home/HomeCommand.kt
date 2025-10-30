package me.clearedSpore.sporeCore.commands.home

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.homes.HomeService
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.menu.homes.HomesMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.TeleportService.awaitTeleport
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("home")
class HomeCommand() : BaseCommand() {

    @Dependency
    lateinit var homeService: HomeService

    @Default
    @CommandCompletion("@homes")
    @Syntax("[home number/name]")
    fun onHome(player: Player, @Optional homeArg: String?) {
        val user = UserManager.get(player)
        if (user == null) {
            return player.userJoinFail()
        }

        if (homeArg.isNullOrEmpty()) {
            HomesMenu(player).open(player)
            return
        }

        if (homeArg.equals("delete", ignoreCase = true)) {
            player.sendErrorMessage("Usage: /home delete <name>")
            return
        }

        val home = homeArg.toIntOrNull()?.let { number ->
            homeService.getAllHomes(user).getOrNull(number - 1)
        } ?: homeService.getHome(user, homeArg)

        if (home == null) {
            player.sendErrorMessage("Home '$homeArg' not found.")
            return
        }

        player.awaitTeleport(home.location)
        player.sendSuccessMessage("Teleported to home '${home.name}'.")
    }

    @Subcommand("delete")
    @CommandCompletion("@homes")
    @Syntax("<name>")
    fun onDelete(player: Player, name: String) {
        val user = UserManager.get(player)
        if (user == null) {
            return player.userJoinFail()
        }

        val home = user.homes.find { input -> input.name.equals(name) }

        if (home == null) {
            player.sendErrorMessage("That home does not exist!")
            return
        }

        ConfirmMenu(player) {
            homeService.deleteHome(user, name)
            player.sendSuccessMessage("Deleted home '$name'.")
        }.open(player)
    }

    @Subcommand("admin goto")
    @CommandPermission(Perm.ADMIN)
    @CommandCompletion("@players @playerhomes")
    @Syntax("<player> <home>")
    fun adminGoto(sender: Player, targetName: String, homeName: String) {
        val offlinePlayer = Bukkit.getOfflinePlayer(targetName)

        val user = UserManager.get(offlinePlayer.uniqueId)

        if(user == null){
            sender.userJoinFail()
            return
        }

        val home = homeService.getHome(user, homeName)

        if (home == null) {
            sender.sendErrorMessage("Home '$homeName' not found for ${offlinePlayer.name ?: targetName}.")
            return
        }

        sender.teleport(home.location)
        sender.sendSuccessMessage("Teleported to ${offlinePlayer.name ?: targetName}'s home '${home.name}'.")
    }


    @Subcommand("admin delete")
    @CommandPermission(Perm.ADMIN)
    @CommandCompletion("@players @playerhomes")
    @Syntax("<player> <home>")
    fun adminDelete(sender: Player, targetName: String, homeName: String) {
        val offlinePlayer = Bukkit.getOfflinePlayer(targetName)
        if (offlinePlayer == null) {
            sender.sendErrorMessage("Player '$targetName' does not exist.")
            return
        }

        val user = UserManager.get(offlinePlayer.uniqueId)

        if(user == null){
            sender.userJoinFail()
            return
        }

        val home = user.homes.find { input -> input.name.equals(name) }

        if (home == null) {
            sender.sendErrorMessage("That home does not exist!")
            return
        }

        homeService.deleteHome(user, homeName)
        sender.sendSuccessMessage("Deleted home '$homeName' for ${offlinePlayer.name ?: targetName}.")
    }
}
