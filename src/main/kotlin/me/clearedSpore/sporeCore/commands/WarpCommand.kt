package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.features.warp.WarpService
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.menu.warps.WarpsMenu
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.TeleportService.awaitTeleport
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("warp|warps")
class WarpCommand : BaseCommand() {

    @Dependency
    lateinit var warpService: WarpService

    @Default
    @CommandCompletion("@warps")
    @Syntax("<warp>")
    fun warp(player: Player, @Optional name: String?) {
        val warps = warpService.getAllWarps()

        if (name.isNullOrEmpty()) {
            WarpsMenu(player).open(player)
            return
        }

        val warp = warps.find { it.name.equals(name, ignoreCase = true) }

        if (warp == null) {
            player.sendErrorMessage("There was no warp found with that name!")
            return
        }

        if (warp.permission != null && !player.hasPermission(warp.permission)) {
            player.sendErrorMessage("You don't have permission to use this warp!")
            return
        }

        player.awaitTeleport(warp.location)
    }

    @Subcommand("create")
    @CommandCompletion("@warps")
    @CommandPermission(Perm.WARP_CREATE)
    @Syntax("<name>")
    fun createWarp(player: Player, name: String){
        val location = player.location
        val warps = warpService.getAllWarps()
        val warp = warps.find { it.name.equals(name, ignoreCase = true) }

        if(warp != null){
            player.sendErrorMessage("There is already a warp with that name!")
            return
        }

        warpService.createWarp(name, location)
        player.sendSuccessMessage("You have successfully created a new warp called '$name'")
        Logger.log(player, Perm.LOG, "created a new warp called '$name'", false)
    }


    @Subcommand("delete")
    @CommandCompletion("@warps")
    @Syntax("<name>")
    @CommandPermission(Perm.WARP_DELETE)
    fun deleteWarp(player: Player, name: String) {
        val warps = warpService.getAllWarps()

        val warp = warps.find { it.name.equals(name, ignoreCase = true) }

        if (warp != null) {
            ConfirmMenu(player){
                warpService.deleteWarp(name)
                player.sendSuccessMessage("Warp '$name' has been deleted.")
            }.open(player)
        } else {
            player.sendErrorMessage("No warp found with the name '$name'.")
        }

        Logger.log(player, Perm.LOG, "deleted a warp called '$name'", false)
    }

    @Subcommand("permission")
    @CommandPermission(Perm.WARP_PERMISSION)
    @CommandCompletion("set|get|clear @warps")
    @Syntax("<set|get|clear> <warp> [permission]")
    fun permission(player: Player, action: String, name: String, @Optional permission: String?) {
        val warps = warpService.getAllWarps()
        val warp = warps.find { it.name.equals(name, ignoreCase = true) }

        if (warp == null) {
            player.sendErrorMessage("No warp found with the name '$name'.")
            return
        }

        when (action.lowercase()) {
            "set" -> {
                if (permission.isNullOrBlank()) {
                    player.sendErrorMessage("You must specify a permission to set.")
                    return
                }

                warpService.setPermission(name, permission)
                player.sendSuccessMessage("Set permission for warp '$name' to '$permission'.")
            }

            "get" -> {
                val current = warp.permission
                if (current != null) {
                    player.sendSuccessMessage("Warp '$name' requires permission: §f$current")
                } else {
                    player.sendSuccessMessage("Warp '$name' has no permission set.")
                }
            }

            "clear" -> {
                warpService.deletePermission(name)
                player.sendSuccessMessage("Cleared permission for warp '$name'.")
            }

            else -> {
                player.sendErrorMessage("Invalid action. Use §fset|get|clear")
            }
        }
    }

    @Subcommand("reload")
    @CommandPermission(Perm.ADMIN)
    fun reload(sender: CommandSender) {
        try {
            warpService.reloadWarps()
            sender.sendMessage("You have reloaded all warps.".blue())
            Logger.info("Warps reloaded by ${sender.name}")
        } catch (ex: Exception) {
            sender.sendMessage("Failed to reload warps: ${ex.message}".red())
            ex.printStackTrace()
        }
    }

}