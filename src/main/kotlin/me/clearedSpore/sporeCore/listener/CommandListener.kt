package me.clearedSpore.sporeCore.listener

import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.AutoListener
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.config.PunishmentLogConfig
import me.clearedSpore.sporeCore.features.setting.impl.PunishmentLogsSetting
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

@AutoListener
class CommandListener : Listener {

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val sender = event.player
        val command = event.message
        val punishConfig = PunishmentService.config
        val user = UserManager.get(sender)

        if(user == null){
            sender.sendErrorMessage("Failed to load your user data! Please retry in a few seconds.\n If this issue keeps happening contact an administrator!")
            event.isCancelled = true
            return
        }

        if(user.isMuted() && punishConfig.settings.blockedMutedCommands.contains(command.lowercase())){
            sender.sendErrorMessage("You can't run that command while being muted!")
            event.isCancelled = true
            return
        }

        if (SporeCore.instance.coreConfig.logs.commands) {
            LogsService.addLog(sender.uuidStr(), command, LogType.COMMAND)
        }
    }
}