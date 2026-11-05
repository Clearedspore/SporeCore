package me.clearedSpore.sporeCore.commands

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.features.reboot.RebootService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender


@CommandAlias("reboot|restart")
@CommandPermission(Perm.REBOOT)
@RegisterCommand
class RebootCommand() : SporeCommand() {


    @Default
    @Syntax("<time>")
    fun onReboot(sender: CommandSender, time: String) {
        RebootService.startReboot(time)
    }

    @Subcommand("cancel")
    fun onCancel(sender: CommandSender) {
        RebootService.cancelReboot()
    }
}