package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender


@CommandAlias("broadcast|bc|alert|announce")
@CommandPermission(Perm.BROADCAST)
@SporeCoreCommand
class BroadcastCommand : BaseCommand() {

    private val quoteRegex = "\"([^\"]+)\"".toRegex()

    @Default
    @Syntax("<message|predefined> [args...]")
    @CommandCompletion("@predefinedBroadcasts")
    fun onBroadcast(sender: CommandSender, vararg messageParts: String) {
        if (messageParts.isEmpty()) {
            sender.sendMessage("Usage: /broadcast <message|predefined> [args...]".red())
            return
        }

        val config = SporeCore.instance.coreConfig.broadcastConfig
        val prefix = config.broadcastPrefix
        val predefined = config.predefinedBroadcasts

        val key = messageParts[0].lowercase()

        if (predefined.containsKey(key)) {
            val template = predefined[key]!!

            val input = messageParts.drop(1).joinToString(" ")
            val args = mutableListOf<String>()

            val regex = "\"([^\"]+)\"|(\\S+)".toRegex()
            regex.findAll(input).forEach {
                args.add(it.groups[1]?.value ?: it.groups[2]!!.value)
            }

            val placeholderCount = "\\{\\d+}".toRegex().findAll(template).count()
            if (args.size < placeholderCount) {
                sender.sendMessage("You must provide $placeholderCount arguments for this broadcast.".red())
                sender.sendMessage("Example: /broadcast $key \"arg1\" \"arg2\" ...".blue())
                return
            }

            var message = template
            args.forEachIndexed { index, value ->
                message = message.replace("{$index}", value)
            }

            Message.broadcastMessage(prefix + message.translate())
        } else {
            val message = messageParts.joinToString(" ").translate()
            Message.broadcastMessage(prefix + message)
        }
    }

    @Subcommand("list")
    fun onList(sender: CommandSender) {
        val predefined = SporeCore.instance.coreConfig.broadcastConfig.predefinedBroadcasts
        if (predefined.isEmpty()) {
            sender.sendMessage("No predefined broadcasts found.".red())
            return
        }

        sender.sendMessage("Predefined Broadcasts:".blue())
        predefined.forEach { (key, value) ->
            sender.sendMessage("&e- $key: &f$value".translate())
        }
    }
}