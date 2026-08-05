package me.clearedSpore.sporeCore.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player

@CommandAlias("tphere|teleporthere|bring")
@CommandPermission(Perm.TELEPORT_OTHERS)
@SporeCoreCommand
class TphereCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    fun onTphere(sender: Player, targets: TargetPlayers) {
        var suffix = chatService?.getPlayerSuffix(sender)?.translate() ?: ""
        var targetSuffix = ""

        if (targets.isEmpty()) {
            sender.sendErrorMessage("No valid players.")
            return
        }

        targets.forEach { player ->
            player.teleportAsync(sender.location)
        }

        if (targets.size == 1) {
            if (SporeCore.instance.coreConfig.logs.teleports) {
                targetSuffix = chatService?.getPlayerSuffix(targets.first().player)?.translate() ?: ""
                LogsService.addLog(sender.name, "Teleported $targetSuffix${targets.first().name}&r to them", LogType.TELEPORT)
            }
            Logger.log(suffix, sender, Perm.LOG, "teleported $targetSuffix${targets.first().name}&r to them", false)
            sender.sendSuccessMessage("Teleported ${targets.first().name} to you.")
        } else {
            if (SporeCore.instance.coreConfig.logs.teleports) {
                LogsService.addLog(sender.name, "Teleported ${targets.size} players to them", LogType.TELEPORT)
            }
            Logger.log(suffix, sender, Perm.LOG, "teleported ${targets.size} players to them", false)
            sender.sendSuccessMessage("Teleported ${targets.size} players to you.")
        }

    }
}
