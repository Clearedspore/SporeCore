package me.clearedSpore.sporeCore.commands.home

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.homes.HomeService
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.entity.Player

@CommandAlias("sethome|createhome")
class CreateHomeCommand() : BaseCommand() {

    @Dependency
    lateinit var homeService: HomeService

    @Default
    @Syntax("<name>")
    fun onCreate(player: Player, name: String) {
        val user = UserManager.get(player)

        if(user == null){
            player.userJoinFail()
            return
        }

        val homeCount = user.homes.size + 1
        val permission = "sporecore.home.$homeCount"

        if (!player.hasPermission(permission)) {
            player.sendErrorMessage("You do not have permission to create home #$homeCount.")
            return
        }

        val existingHome = homeService.getAllHomes(user).find { it.name.equals(name, ignoreCase = true) }
        if (existingHome != null) {
            player.sendErrorMessage("You already have a home with that name!")
            return
        }

        homeService.createHome(user, name, player.location)
        player.sendSuccessMessage("Created home '$name'.")
    }
}
