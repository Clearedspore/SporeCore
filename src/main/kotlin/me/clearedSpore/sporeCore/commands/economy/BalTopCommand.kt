package me.clearedSpore.sporeCore.commands.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.menu.baltop.BalTopMenu
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("baltop|ecotop")
@CommandPermission(Perm.ECO)
class BalTopCommand: BaseCommand() {

    @Default
    fun onTop(sender: CommandSender, @Optional page: Int?) {
        if (!EconomyService.isReady()) {
            sender.sendMessage("Economy system is not ready.".red())
            return
        }

        val useMenu = SporeCore.instance.coreConfig.economy.topMenu
        if (useMenu && sender is Player) {
            BalTopMenu(sender).open(sender)
            return
        }

        val requestedPage = (page ?: 1).coerceAtLeast(1)
        val pageSize = 10
        val startIndex = (requestedPage - 1) * pageSize

        EconomyService.top().thenAccept { topList ->
            if (topList.isEmpty() || startIndex >= topList.size) {
                sender.sendMessage("No more results.".red())
                return@thenAccept
            }

            val endIndex = minOf(startIndex + pageSize, topList.size)
            sender.sendMessage("=== Balance Top ===".blue())

            topList.subList(startIndex, endIndex).forEachIndexed { index, (player, bal) ->
                val displayName = player.name.takeIf { it!!.isNotEmpty() } ?: player.uniqueId.toString()
                sender.sendMessage(
                    "#${startIndex + index + 1}".white() + " " +
                            displayName.blue() + " " +
                            EconomyService.format(bal).green()
                )
            }
        }.exceptionally { ex ->
            sender.sendMessage("Failed to load top balances.".red())
            ex.printStackTrace()
            null
        }
    }

}