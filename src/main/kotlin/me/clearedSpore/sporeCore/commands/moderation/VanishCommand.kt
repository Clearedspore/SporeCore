package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Name
import co.aikar.commands.annotation.Optional
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

@CommandAlias("vanish|v")
@CommandPermission(Perm.VANISH)
class VanishCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    fun onVanish(sender: CommandSender, @Optional @Name("target") target: OnlinePlayer?){
        if(sender is ConsoleCommandSender && target == null){
            sender.sendMessage("Console must support a target!".red())
            return
        }

        if(target == null){
            val player = Bukkit.getPlayer(sender.name)
            if(player == null){
                return
            }

            VanishService.toggle(player.uniqueId)
            val isVanished = VanishService.isVanished(player.uniqueId)
            player.sendMessage(if (isVanished) "Enabled Vanish".blue() else "Disabled Vanish".red())
            Logger.log(sender, Perm.LOG, if (isVanished) "Enabled Vanish" else "Disabled Vanish", false)
        } else {
            if(!sender.hasPermission(Perm.VANISH_OTHERS)){
                sender.sendMessage("You don't have permission to toggle other players their vanish!".red())
                return
            }

            VanishService.toggle(target.player.uniqueId)
            val isVanished = VanishService.isVanished(target.player.uniqueId)
            sender.sendMessage(if (isVanished) "Enabled Vanish for ${target.player.name}".blue() else "Disabled Vanish for ${target.player.name}".red())
            Logger.log(sender, Perm.LOG,if (isVanished) "Enabled Vanish for ${target.player.name}" else "Disabled Vanish for ${target.player.name}", false)
        }
    }
}