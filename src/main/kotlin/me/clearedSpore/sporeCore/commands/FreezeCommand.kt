package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.metadata.FixedMetadataValue

@CommandAlias("freeze")
@CommandPermission(Perm.FREEZE)
@SporeCoreCommand
class FreezeCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    fun onFreeze(sender: CommandSender, @Name("target") targetRaw: OnlinePlayer) {
        val target = targetRaw.player

        val shouldLog = SporeCore.instance.coreConfig.logs.freeze

        if (target.hasMetadata("frozen")) {
            target.removeMetadata("frozen", SporeCore.instance)

            Logger.log(sender, Perm.LOG, "unfrozen ${target.name}", true)
            if (shouldLog) {
                LogsService.addLog(sender.uuidStr(), "Unfroze ${target.name}", LogType.FREEZE)
                LogsService.addLog(target.uuidStr(), "Unfronzen by ${sender.name}", LogType.FREEZE)
            }

            target.sendMessage("")
            target.sendMessage("")
            target.sendMessage("You are no longer frozen!!".blue())
            target.sendMessage("You may leave the server again.".blue())
            target.sendMessage("")
            target.sendMessage("")
        } else {
            target.setMetadata("frozen", FixedMetadataValue(SporeCore.instance, true))

            Logger.log(sender, Perm.LOG, "froze ${target.name}", true)
            if (shouldLog) {
                LogsService.addLog(sender.uuidStr(), "Froze ${target.name}", LogType.FREEZE)
                LogsService.addLog(target.uuidStr(), "Frozen by ${sender.name}", LogType.FREEZE)
            }

            target.sendMessage("")
            target.sendMessage("")
            target.sendMessage("You have been &bFrozen".red())
            target.sendMessage("Do not leave the server!".red())
            target.sendMessage("Wait for further instructions!".red())
            target.sendMessage("")
            target.sendMessage("")
        }
    }


    @Private
    @Subcommand("set")
    fun onSet(sender: CommandSender, targetRaw: OnlinePlayer, state: Boolean) {
        val target = targetRaw.player

        val shouldLog = SporeCore.instance.coreConfig.logs.freeze

        if (state == false) {
            target.removeMetadata("frozen", SporeCore.instance)
            Logger.log(sender, Perm.LOG, "unfrozen ${target.name}", true)
            if (shouldLog) {
                LogsService.addLog(sender.uuidStr(), "Unfroze ${target.name}", LogType.FREEZE)
                LogsService.addLog(target.uuidStr(), "Unfronzen by ${sender.name}", LogType.FREEZE)
            }

            target.sendMessage("")
            target.sendMessage("")
            target.sendMessage("You are no longer frozen!!".blue())
            target.sendMessage("You may leave the server again.".blue())
            target.sendMessage("")
            target.sendMessage("")
        } else {
            target.setMetadata("frozen", FixedMetadataValue(SporeCore.instance, true))
            Logger.log(sender, Perm.LOG, "froze ${target.name}", true)

            if (shouldLog) {
                LogsService.addLog(sender.uuidStr(), "Froze ${target.name}", LogType.FREEZE)
                LogsService.addLog(target.uuidStr(), "Frozen by ${sender.name}", LogType.FREEZE)
            }

            target.sendMessage("")
            target.sendMessage("")
            target.sendMessage("You have been &bFrozen".red())
            target.sendMessage("Do not leave the server!".red())
            target.sendMessage("Wait for further instructions!".red())
            target.sendMessage("")
            target.sendMessage("")
        }
    }
}