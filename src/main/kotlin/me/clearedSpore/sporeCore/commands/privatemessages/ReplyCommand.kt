package me.clearedSpore.sporeCore.commands.privatemessages

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
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
import me.clearedSpore.sporeCore.util.Util.noTranslate
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player


@CommandAlias("reply|r")
class ReplyCommand : BaseCommand() {

    @Default
    @Syntax("<message>")
    fun onReply(player: Player, messageParts: String) {
        val message: String = messageParts.joinWithSpaces()
        val lastSenderId = PMService.getLastSender(player)

        if (Cooldown.isOnCooldown("msg_cooldown", player.uniqueId)) {
            player.sendErrorMessage("Please wait before doing that again")
            return
        }

        if (lastSenderId == null) {
            player.sendErrorMessage("You don't have anyone to reply to!")
            return
        }

        val target = Bukkit.getPlayer(lastSenderId)
        if (target == null || !target.isOnline) {
            player.sendErrorMessage("Your last sender is not online.")
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
        player.sendMessage("You » ${target.name} &f".blue() + message.noTranslate())
        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f)

        target.sendMessage("${player.name} » You &f".blue() + message.noTranslate())
        target.playSound(target.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)
    }
}
