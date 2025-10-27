package me.clearedSpore.sporeCore.commands.privatemessages

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Cooldown
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.StringUtil.joinWithSpaces
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.user.settings.Setting
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player


@CommandAlias("whisper|pm|msg")
class PrivateMessageCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> <message>")
    fun onMessage(player: Player, targetName: String, messageParts: String) {
        val message: String = messageParts.joinWithSpaces()
        val target = Bukkit.getPlayer(targetName)

        if (Cooldown.isOnCooldown("msg_cooldown", player.uniqueId)) {
            player.sendErrorMessage("Please wait before doing that again")
            return
        }


        if (target == null) {
            player.sendErrorMessage("That player is not online!")
            return
        }

        val user = UserManager.get(target)

        if(user == null){
            player.userJoinFail()
            return
        }

        if (!user.isSettingEnabled(Setting.PRIVATE_MESSAGES) && !player.hasPermission(Perm.PM_BYPASS)) {
            player.sendErrorMessage("That player has private messages disabled!".red())
            return
        }

        PMService.setLastSender(player, target)

        Cooldown.addCooldown("msg_cooldown", player.uniqueId)
        player.sendMessage("You » ${target.name} ".blue() + message.white())
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f)

        target.sendMessage("${player.name} » You ".blue() + message.white())
        target.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
    }
}
