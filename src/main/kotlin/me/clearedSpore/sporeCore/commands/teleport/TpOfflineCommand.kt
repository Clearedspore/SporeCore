package me.clearedSpore.sporeCore.commands.teleport

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.annotation.*
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("tpo|tpoffline")
@CommandPermission(Perm.TELEPORT)
@RegisterCommand()
class TpOfflineCommand : SporeCommand() {

    @Default
    @CommandPermission(Perm.TELEPORT + ".offline")
    fun onTpOffline(sender: CommandSender, target: OfflinePlayer) {
        var suffix = if (sender is Player) chatService?.getPlayerSuffix(sender)?.translate() ?: "" else ""
        val targetPlayer = Bukkit.getOfflinePlayer(target.uniqueId)

        val shouldLog = SporeCore.instance.coreConfig.logs.teleports

        if (sender !is Player) {
            sender.sendErrorMessage("Only a player can use this command")
            return
        }
        val user = UserManager.get(targetPlayer)

        if (user == null) {
            sender.sendErrorMessage("That player does not exist.")
            return
        }

        if (target.isOnline) {
            sender.chat("/tp ${target.name}")
            return
        }

        if (user.lastLocation == null) {
            sender.sendErrorMessage("That user doesn't have a last location to teleport to.")
            return
        }

        sender.teleportAsync(user.lastLocation!!)
        sender.sendSuccessMessage("Teleported to offline player ${targetPlayer.name}.")
        Logger.log(suffix, sender, Perm.LOG, "teleported to offline player ${targetPlayer.name}", false)
        LogsService.addLog(sender.uuidStr(), "Teleported to offline player ${target.name}", LogType.TELEPORT)
    }
}
