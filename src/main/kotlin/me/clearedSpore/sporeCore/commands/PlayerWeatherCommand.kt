package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.StringUtil.capitalizeFirstLetter
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.WeatherType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("pweather")
@CommandPermission(Perm.PWEATHER)
class PlayerWeatherCommand : BaseCommand() {

    @Default
    @CommandCompletion(" @players")
    @Syntax("<weather> <player>")
    fun onPweather(sender: CommandSender, type: WeatherType, @Optional targetName: String?) {

        val target: Player? = when {
            sender is Player && targetName == null -> sender
            targetName != null -> Bukkit.getPlayer(targetName)
            else -> null
        }

        if (target == null) {
            sender.sendMessage("That player is not online!".red())
            return
        }

        if (sender != target && !sender.hasPermission(Perm.PWEATHER_OTHERS)) {
            sender.sendMessage("You don't have permission to repair other players' items.".red())
            return
        }

        target.setPlayerWeather(type)

        if(target == sender){
            sender.sendMessage("Your personal weather has been set to ${type.toString().capitalizeFirstLetter()}".blue())
        } else {
            sender.sendMessage("You have set ${targetName}'s weather to ${type.toString().capitalizeFirstLetter()}".blue())
            target.sendMessage("Your personal weather has been set to ${type.toString().capitalizeFirstLetter()}".blue())
        }
    }
}
