package me.clearedSpore.sporeCore.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("tpall|teleportall")
@CommandPermission(Perm.TELEPORT_ALL)
@SporeCoreCommand
class TpAllCommand : BaseCommand() {

    @Default
    fun onTpAll(sender: Player) {
        Bukkit.getOnlinePlayers().forEach {
            if (it != sender) it.teleport(sender.location)
        }

        val amount = Bukkit.getOnlinePlayers().size - 1

        Logger.log(sender, Perm.LOG, "teleported $amount players themself", false)
        if (SporeCore.instance.coreConfig.logs.teleports) {
            LogsService.addLog(sender.uuidStr(), "Teleported $amount players to them", LogType.TELEPORT)
        }
        sender.sendSuccessMessage("Teleported everyone to you.")
    }
}
