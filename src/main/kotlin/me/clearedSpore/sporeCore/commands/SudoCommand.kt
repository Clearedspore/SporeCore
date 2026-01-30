package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("sudo")
@CommandPermission(Perm.SUDO)
@SporeCoreCommand
class SudoCommand : BaseCommand() {

    @Default()
    @Syntax("<player|*> <command|message(chat:)>")
    @CommandCompletion("@players|*")
    fun onSudo(sender: CommandSender, targetName: String, action: String) {

        val targets: List<Player> = if (targetName == "*") {
            Bukkit.getOnlinePlayers().toList()
        } else {
            val target = Bukkit.getPlayerExact(targetName)
            if (target == null) {
                sender.userJoinFail()
                return
            }
            listOf(target)
        }


        targets.forEach { target ->
            if (action.startsWith("chat:")) {
                val message = action.removePrefix("chat:")
                target.chat(message)
            } else {
                target.performCommand(action)
            }
        }

        if (targets.size == 1) {
            val t = targets[0]
            if (action.startsWith("chat:")) {
                sender.sendMessage("Successfully forced ${t.name} to send a chat message".blue())
            } else {
                sender.sendMessage("Successfully forced ${t.name} to run a command".blue())
            }
        } else {
            if (action.startsWith("chat:")) {
                sender.sendMessage("Successfully forced ${targets.size} players to send a chat message".blue())
            } else {
                sender.sendMessage("Successfully forced ${targets.size} players to run a command".blue())
            }
        }
    }
}