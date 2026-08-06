package me.clearedSpore.sporeCore.commands.teleport

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("tpall|teleportall")
@CommandPermission(Perm.TELEPORT_ALL)
@RegisterCommand
class TpAllCommand : SporeCommand() {

    @Default
    fun onTpAll(sender: Player) {
        var suffix = chatService?.getPlayerSuffix(sender)?.translate() ?: ""
        Bukkit.getOnlinePlayers().forEach {
            if (it != sender) it.teleportAsync(sender.location)
        }

        val amount = Bukkit.getOnlinePlayers().size - 1

        Logger.log(suffix, sender, Perm.LOG, "teleported $amount players themself", false)
        if (SporeCore.instance.coreConfig.logs.teleports) {
            LogsService.addLog(sender.uuidStr(), "Teleported $amount players to them", LogType.TELEPORT)
        }
        sender.sendSuccessMessage("Teleported everyone to you.")
    }
}
