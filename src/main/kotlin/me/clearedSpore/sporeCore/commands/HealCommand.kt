package me.clearedSpore.sporeCore.commands

import me.clearedSpore.sporeAPI.command.SporeCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeAPI.annotation.RegisterCommand
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

@CommandAlias("heal")
@CommandPermission(Perm.HEAL)
@RegisterCommand
class HealCommand : SporeCommand() {

    @Default
    @CommandCompletion("@targets")
    fun onHeal(sender: CommandSender, @Optional targets: TargetPlayers?) {
        var suffix = if (sender is Player) chatService?.getPlayerSuffix(sender)?.translate() ?: "" else "&4"

        val resolved = targets ?: when (sender) {
            is Entity -> listOf(sender)
            else -> throw InvalidCommandArgument("You must specify a target.")
        }

        val healables = resolved.filterIsInstance<Damageable>().filter {
            it !is Player || sender == it || sender.hasPermission(Perm.HEAL_OTHERS)
        }

        if (healables.isEmpty()) {
            throw InvalidCommandArgument("No valid entities to heal.")
        }

        healables.forEach {
            it.health = it.maxHealth
            if (it is Player) {
                it.foodLevel = 20
                it.saturation = 20f
            }
        }

        val count = healables.size

        if (count == 1) {
            val name = (healables.first() as? Player)?.name ?: healables.first().type.name.lowercase()
            val targetSuffix = chatService?.getPlayerSuffix(healables.first() as? Player)?.translate()
            sender.sendMessage("You healed $targetSuffix$name&r&#1D91FF.".blue())
            Logger.log(suffix, sender, Perm.LOG, "healed $targetSuffix$name", false)
        } else {
            sender.sendMessage("You healed $count entities.".blue())
            Logger.log(suffix, sender, Perm.LOG, "healed $count entities", false)
        }


    }
}
