package me.clearedSpore.sporeCore.commands.spawn

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player


@CommandAlias("setspawn")
@CommandPermission(Perm.SETSPAWN)
class SetSpawnCommand() : BaseCommand() {


    @Default()
    fun onSetSpawn(player: Player) {
        val serverData = DatabaseManager.getServerData()
        serverData.spawnString =
            "${player.world.name},${player.location.x},${player.location.y},${player.location.z},${player.location.yaw},${player.location.pitch}"
        player.sendSuccessMessage("You have successfully set the spawn location!")
        DatabaseManager.saveServerData()
    }


}