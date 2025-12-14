package me.clearedSpore.sporeCore.commands.moderation.mode

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.features.mode.item.ModeItemManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

@CommandAlias("staffmode|modmode|mode")
@CommandPermission(Perm.MODE_ALLOW)
class ModeCommand : BaseCommand() {

    @Default
    fun onModeToggle(
        sender: CommandSender,
        @Optional @Name("mode") modeId: String?,
        @Optional @Name("target") target: OnlinePlayer?
    ) {
        val service = ModeService

        if (sender is ConsoleCommandSender && modeId != null && target == null) {
            sender.sendMessage("Console must provide a target!")
            return
        }

        if (modeId == null && target == null) {
            if (sender !is org.bukkit.entity.Player) {
                sender.sendMessage("Console must provide a target!")
                return
            }

            val player = sender
            val highest = service.getHighestMode(player)

            if (highest == null) {
                player.sendErrorMessage("You do not have access to any modes!")
                return
            }

            val enabled = service.isInMode(player)
            val status = if (enabled == true) "Disabled" else "Enabled"


            service.toggleMode(player, highest.id)
            player.sendMessage("$status ${highest.name} mode".blue())
            Logger.log(sender, Perm.LOG, "$status ${highest.name} mode", false)
            return
        }

        if (modeId != null && target == null) {
            if (sender !is org.bukkit.entity.Player) {
                sender.sendMessage("Console must provide a target!")
                return
            }

            val player = sender
            val mode = service.getModeById(modeId)

            if (mode == null) {
                player.sendErrorMessage("Invalid mode id: $modeId")
                return
            }

            if (!player.hasPermission(mode.permission)) {
                player.sendErrorMessage("You don't have permission to toggle this mode!")
                return
            }

            val enabled = service.isInMode(player)
            val status = if (enabled == true) "Disabled" else "Enabled"

            service.toggleMode(player, mode.id)
            player.sendMessage("$status ${mode.name} mode".blue())
            Logger.log(sender, Perm.LOG, "$status ${mode.name}", false)
            return
        }

        if (modeId != null && target != null) {
            val mode = service.getModeById(modeId)

            if (mode == null) {
                sender.sendMessage("Invalid mode id: $modeId".red())
                return
            }

            if (!sender.hasPermission(Perm.MODE_OTHERS) && !sender.hasPermission(mode.permission)) {
                sender.sendMessage("You don't have permission to toggle that mode for another player!".red())
                return
            }

            val enabled = service.isInMode(target.player)
            val status = if (enabled == true) "Disabled" else "Enabled"

            service.toggleMode(target.player, mode.id)
            sender.sendMessage("$status ${mode.name} mode for ${target.player.name}".blue())
            Logger.log(sender, Perm.LOG, "$status ${mode.name} mode for ${target.player.name}", false)
            return
        }
    }

    @Subcommand("itemlist")
    @CommandPermission(Perm.ADMIN)
    fun onItemList(sender: CommandSender){
        sender.sendMessage("Mode items list:".blue())
        ModeItemManager.getAllItems().forEach { item ->
            sender.sendMessage("&7- &f${item.id}".translate())
        }
    }

    @Subcommand("disableall")
    @CommandPermission(Perm.ADMIN)
    fun onDisableAll(sender: CommandSender) {
        val service = ModeService

        if (service.activeModes.isEmpty()) {
            sender.sendMessage("No one is currently in any mode!".red())
            return
        }

        val active = service.activeModes.size

        service.disableAll()
        sender.sendMessage("Disabled any active mode for $active player(s).")
    }
}
