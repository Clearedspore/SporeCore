package me.clearedSpore.sporeCore.commands.spawn

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.util.TeleportService.awaitTeleport
import org.bukkit.entity.Player


@CommandAlias("spawn|gotospawn")
class SpawnCommand() : BaseCommand() {


    @Default
    fun onSpawn(player: Player) {
        val spawn = DatabaseManager.getServerData().spawn

        if (spawn == null) {
            player.sendMessage("Spawn has not been set yet!".red())
            return
        }

        player.awaitTeleport(spawn)
    }
}