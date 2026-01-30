package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player

@CommandAlias("tpsbar")
@CommandPermission(Perm.TPSBAR)
@SporeCoreCommand
class TPSBarCommand : BaseCommand() {

    @Default
    fun onTpsBar(player: Player) {
        val user = UserManager.get(player)
        if (user == null) {
            player.userFail()
            return
        }

        val current = user.tpsBar
        user.tpsBar = !current
        player.sendSuccessMessage("Toggled TPS bar!")

    }
}