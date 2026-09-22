package me.clearedSpore.sporeCore.commands.privatemessages

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.features.setting.impl.SocialSpySetting
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("socialspy")
@CommandPermission(Perm.PM_BYPASS)
class SocialSpyCommand : BaseCommand() {

    @Default
    fun onSocialSpy(sender: CommandSender) {
        if (sender !is Player) sender.sendErrorMessage("Only players can use this command.")
        val user = UserManager.get(sender.name) ?: return

        if (!user.getSettingOrDefault(SocialSpySetting())) {
            user.setSetting(SocialSpySetting(), true)
            sender.sendSuccessMessage("You have enabled socialspy.")
            user.save()
        } else {
            user.setSetting(SocialSpySetting(), false)
            sender.sendSuccessMessage("You have disabled socialspy.")
            user.save()
        }

    }
}