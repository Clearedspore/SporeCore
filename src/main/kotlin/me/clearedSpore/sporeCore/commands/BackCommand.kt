package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.TeleportService.awaitTeleport
import org.bukkit.entity.Player


@CommandAlias("back")
@CommandPermission(Perm.BACK)
@SporeCoreCommand
class BackCommand : BaseCommand() {

    @Default
    fun onBack(player: Player) {
        val user = UserManager.get(player)
        if (user == null) {
            player.userFail()
            return
        }

        val currentLocation = player.location
        val lastLocation = user.lastLocation

        if (lastLocation == null) {
            player.sendErrorMessage("You don't have any locations to teleport to!")
            return
        }

        player.awaitTeleport(lastLocation)
        user.lastLocation = currentLocation
    }
}