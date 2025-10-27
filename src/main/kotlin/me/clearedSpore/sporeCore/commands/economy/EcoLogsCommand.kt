package me.clearedSpore.sporeCore.commands.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("ecologs|economylogs")
@CommandPermission(Perm.ECO_ADMIN)
class EcoLogsCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> [page]")
    fun onLogs(sender: CommandSender, targetName: String, @Optional page: Int?) {
        val logPage = page ?: 1
        val user = UserManager.get(Bukkit.getOfflinePlayer(targetName).uniqueId)

        if(user == null){
            sender.userJoinFail()
            return
        }


        user.getEconomyLogs(logPage, 10).thenAccept { logs ->


            if (logs.isEmpty()) {
                sender.sendMessage("No logs found.".red())
                return@thenAccept
            }

            val displayName = user.playerName.ifEmpty { targetName }
            sender.sendMessage("=== Economy Logs for $displayName (Page $logPage) ===".blue())
            logs.forEach { sender.sendMessage(it.translate()) }
        }
    }
}
