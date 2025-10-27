package me.clearedSpore.sporeCore.commands.home

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.homes.HomeService
import me.clearedSpore.sporeCore.menu.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.entity.Player

@CommandAlias("delhome|deletehome")
class DelHomeCommand() : BaseCommand() {

    @Dependency
    lateinit var homeService: HomeService

    @Default
    @CommandCompletion("@homes")
    @Syntax("<name>")
    fun onDelete(player: Player, name: String) {
        val user = UserManager.get(player)

        if(user == null){
            player.userJoinFail()
            return
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
}

