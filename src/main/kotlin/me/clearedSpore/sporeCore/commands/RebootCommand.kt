package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.features.reboot.RebootService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender


@CommandAlias("reboot|restart")
@CommandPermission(Perm.REBOOT)
@SporeCoreCommand
class RebootCommand() : BaseCommand() {


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