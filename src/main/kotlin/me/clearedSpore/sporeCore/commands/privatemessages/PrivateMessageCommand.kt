package me.clearedSpore.sporeCore.commands.privatemessages

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Cooldown
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.StringUtil.joinWithSpaces
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.setting.impl.PrivateMessagesSetting
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Util.noTranslate
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player


@CommandAlias("whisper|pm|msg|tell")
class PrivateMessageCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> <message>")
    fun onMessage(player: Player, targetName: String, messageParts: String) {
        var suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""
        val message: String = messageParts.joinWithSpaces()
        val target = Bukkit.getPlayer(targetName)
        var targetSuffix = chatService?.getPlayerSuffix(target)?.translate() ?: ""

        if (Cooldown.isOnCooldown("msg_cooldown", player.uniqueId)) {
            player.sendErrorMessage("Please wait before doing that again")
            return
        }


        if (target == null) {
            player.sendErrorMessage("That player is not online!")
            return
        }

        val user = UserManager.get(target)

        if (user == null) {
            player.userJoinFail()
            return
        }

        if (!user.getSettingOrDefault(PrivateMessagesSetting()) && !player.hasPermission(Perm.PM_BYPASS)) {
            player.sendErrorMessage("That player has private messages disabled!".red())
            return
        }

        PMService.setLastSender(player, target)

        Cooldown.addCooldown("msg_cooldown", player.uniqueId)
        player.sendMessage("You » $targetSuffix${target.name}&r&#1D91FF: &f".blue() + message.noTranslate())
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f)

        target.sendMessage("$suffix${player.name}&r&#1D91FF » You: &f".blue() + message.noTranslate())
        target.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)

        if (SporeCore.instance.coreConfig.logs.privateMessages) {
            LogsService.addLog(
                player.uuidStr(),
                "to ${target.name}: ${message.noTranslate()}",
                LogType.PRIVATE_MESSAGE
            )

            LogsService.addLog(
                target.uuidStr(),
                "from ${player.name}: ${message.noTranslate()}",
                LogType.PRIVATE_MESSAGE
            )
        }

    }
}
