package me.clearedSpore.sporeCore.commands.util

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.util.Perm

@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@RegisterCommand
class UtilCommand : SporeCommand() {

    @Default
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }
}