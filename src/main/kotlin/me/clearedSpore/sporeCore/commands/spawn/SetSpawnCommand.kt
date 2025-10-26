package me.clearedSpore.sporeCore.commands.spawn

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.database.Database
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player
import javax.xml.crypto.Data


@CommandAlias("setspawn")
@CommandPermission(Perm.SETSPAWN)
class SetSpawnCommand(): BaseCommand() {


    @Default()
    fun onSetSpawn(player: Player){
        DatabaseManager.getServerData().spawn = player.location
        player.sendSuccessMessage("You have successfully set the spawn location!")
    }

}