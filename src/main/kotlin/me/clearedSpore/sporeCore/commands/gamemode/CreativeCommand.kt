package me.clearedSpore.sporeCore.commands.gamemode

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.util.Perm
import net.kyori.adventure.sound.Sound
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("creative|gmc")
@CommandPermission(Perm.CREATIVE)
@SporeCoreCommand
class CreativeCommand : BaseCommand() {

    @Default
    @CommandCompletion("@targets")
    @Syntax("<player>")
    fun onCreative(sender: CommandSender, @Optional targets: TargetPlayers?) {
        var senderSuffix = if (sender is Player) chatService?.getPlayerSuffix(sender)?.translate() ?: "" else "&4"

        val resolved = targets ?: when (sender) {
            is Player -> listOf(sender)
            else -> {
                sender.sendMessage("You must specify a player when running this command from console.".red())
                return
            }
        }

        val players = resolved.filter { sender == it || sender.hasPermission(Perm.GAMEMODE_OTHERS) }

        if (players.isEmpty()) {
            sender.sendMessage("No valid players.".red())
            return
        }

        players.forEach { target ->
            target.gameMode = GameMode.CREATIVE
            var targetSuffix = chatService?.getPlayerSuffix(target)?.translate() ?: ""
            if (sender == target) {
                Logger.log(senderSuffix, sender, Perm.LOG, "&rchanged their gamemode to Creative", false)
                sender.sendMessage("Your gamemode has been updated to Creative".blue())
            } else {
                Logger.log(senderSuffix, sender, Perm.LOG, "&rchanged ${targetSuffix}${target.name}&r&f’s gamemode to Creative", false)
                sender.sendMessage("You updated ".blue() + targetSuffix + target.name + "’s gamemode to Creative.".blue())
                target.player?.sendMessage("Your gamemode has been updated to Creative".blue())
                }
            }
        }
    }
