package me.clearedSpore.sporeCore.commands.util

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm

@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@SporeCoreCommand
class UtilCommand : BaseCommand() {

    @Default
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }
}