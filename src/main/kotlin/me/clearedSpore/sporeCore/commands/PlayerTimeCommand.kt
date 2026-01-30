package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("ptime")
@CommandPermission(Perm.PTIME)
@SporeCoreCommand
class PlayerTimeCommand : BaseCommand() {

    @Default
    @CommandCompletion(" @players")
    @Syntax("<time> <player>")
    fun onPTime(sender: CommandSender, type: Time, @Optional targetName: String?) {

        val target: Player? = when {
            sender is Player && targetName == null -> sender
            targetName != null -> Bukkit.getPlayer(targetName)
            else -> null
        }

        if (target == null) {
            sender.sendMessage("That player is not online!".red())
            return
        }

        if (sender != target && !sender.hasPermission(Perm.PTIME_OTHERS)) {
            sender.sendMessage("You don't have permission to repair other players' items.".red())
            return
        }

        target.setPlayerTime(type.tick, false)


        if (target == sender) {
            sender.sendMessage("Your personal time has been set to ${type.toString().capitalizeFirstLetter()}".blue())
        } else {
            sender.sendMessage(
                "You have set ${target.name}'s personal time to ${
                    type.toString().capitalizeFirstLetter()
                }".blue()
            )
            target.sendMessage("Your personal time has been set to ${type.toString().capitalizeFirstLetter()}".blue())
        }
    }

    enum class Time(val tick: Long) {

        DAY(1000),
        NOON(6000),
        NIGHT(13000),
        MIDNIGHT(18000)
        ;

    }

}
