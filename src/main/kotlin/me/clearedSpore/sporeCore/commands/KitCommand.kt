package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.kit.KitService
import me.clearedSpore.sporeCore.menu.kits.KitsMenu
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("kit|kits")
@CommandPermission(Perm.KITS)
class KitCommand : BaseCommand() {

    @Dependency
    lateinit var kitService: KitService

    @Default
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    fun kit(player: Player, @Optional name: String?) {
        val kits = kitService.getAllKits()

        if (name.isNullOrEmpty()) {
            KitsMenu(player).open(player)
            return
        }

        val kit = kits.find { it.name.equals(name, ignoreCase = true) }

        if (kit == null) {
            player.sendErrorMessage("There was no kit found with that name!")
            return
        }

        if (kit.permission != null && !player.hasPermission(kit.permission)) {
            player.sendErrorMessage("You don't have permission to use this kit!")
            return
        }

        kitService.giveKit(player, name)
    }

    @Subcommand("givekit")
    @CommandCompletion("@kits @targets")
    @Syntax("<kit> [targets]")
    @CommandPermission(Perm.KIT_ADMIN)
    fun onGiveKit(
        player: Player,
        kitName: String,
        @Optional targets: TargetPlayers?
    ) {
        val kit = kitService.getAllKits()
            .find { it.name.equals(kitName, ignoreCase = true) }

        if (kit == null) {
            player.sendErrorMessage("There was no kit found with that name!")
            return
        }

        if (kit.permission != null && !player.hasPermission(kit.permission)) {
            player.sendErrorMessage("You don't have permission to use this kit!")
            return
        }

        val resolvedTargets = targets ?: listOf(player)

        if (resolvedTargets.isEmpty()) {
            player.sendErrorMessage("No valid players selected.")
            return
        }

        resolvedTargets.forEach { target ->
            kitService.giveKit(target, kit.name)

            if (target != player) {
                player.sendSuccessMessage("You gave kit ${kit.name} to ${target.name}!")
            }

            target.sendSuccessMessage("You received kit ${kit.name}!")
        }
    }


    @Subcommand("create")
    @CommandCompletion("@kits")
    @CommandPermission(Perm.KIT_ADMIN)
    @Syntax("<name>")
    fun createKit(player: Player, name: String) {
        var suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""
        val kits = kitService.getAllKits()
        val kit = kits.find { it.name.equals(name, ignoreCase = true) }

        if (kit != null) {
            player.sendErrorMessage("There is already a kit with that name!")
            return
        }

        kitService.createKit(name, player, null)
        player.sendSuccessMessage("You have successfully created a new kit called '$name'")
        Logger.log(suffix, player, Perm.LOG, "created a new kit called '$name'", false)
    }

    @Subcommand("delete")
    @CommandCompletion("@kits")
    @Syntax("<name>")
    @CommandPermission(Perm.KIT_ADMIN)
    fun deleteKit(player: Player, name: String) {
        var suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""
        val kit = kitService.getAllKits().find { it.name.equals(name, ignoreCase = true) }

        if (kit != null) {
            ConfirmMenu(player) {
                kitService.deleteKit(name)
                player.sendSuccessMessage("Kit '$name' has been deleted.")
            }.open(player)
        } else {
            player.sendErrorMessage("No kit found with the name '$name'.")
        }

        Logger.log(suffix, player, Perm.LOG, "deleted a kit called '$name'", false)
    }

    @Subcommand("permission")
    @CommandPermission(Perm.KIT_ADMIN)
    @CommandCompletion("set|get|clear @kits")
    @Syntax("<set|get|clear> <kit> [permission]")
    fun permission(player: Player, action: String, name: String, @Optional permission: String?) {
        val kit = kitService.getAllKits().find { it.name.equals(name, ignoreCase = true) }

        if (kit == null) {
            player.sendErrorMessage("No kit found with the name '$name'.")
            return
        }

        when (action.lowercase()) {
            "set" -> {
                if (permission.isNullOrBlank()) {
                    player.sendErrorMessage("You must specify a permission to set.")
                    return
                }

                kitService.setPermission(name, permission)
                player.sendSuccessMessage("Set permission for kit '$name' to '$permission'.")
            }

            "get" -> {
                val current = kit.permission
                if (current != null) {
                    player.sendSuccessMessage("Kit '$name' requires permission: §f$current")
                } else {
                    player.sendSuccessMessage("Kit '$name' has no permission set.")
                }
            }

            "clear" -> {
                kitService.deletePermission(name)
                player.sendSuccessMessage("Cleared permission for kit '$name'.")
            }

            else -> player.sendErrorMessage("Invalid action. Use §fset|get|clear")
        }
    }

    @Subcommand("setdisplayitem")
    @CommandPermission(Perm.KIT_ADMIN)
    @CommandCompletion("@kits @materials")
    @Syntax("<kit> [item]")
    fun onSetDisplayItem(player: Player, name: String, @Optional materialStr: String?) {
        val kit = kitService.getAllKits().find { it.name.equals(name, ignoreCase = true) }

        if (kit == null) {
            player.sendErrorMessage("No kit found with the name '$name'.")
            return
        }

        val material: Material = if (!materialStr.isNullOrEmpty()) {
            try {
                Material.valueOf(materialStr.uppercase())
            } catch (e: IllegalArgumentException) {
                player.sendErrorMessage("Invalid material: '$materialStr'.")
                return
            }
        } else {
            val itemInHand = player.inventory.itemInMainHand
            if (itemInHand == null || itemInHand.type.isAir) {
                player.sendErrorMessage("You must either specify a material or hold an item in your hand.")
                return
            }
            itemInHand.type
        }

        kitService.setDisplayItem(name, material)
        player.sendSuccessMessage("Set display item for kit '$name' to ${material.name.lowercase()}.")
    }

    @Subcommand("setcooldown")
    @CommandPermission(Perm.KIT_ADMIN)
    @CommandCompletion("@kits")
    @Syntax("<kit> <time>")
    fun onSetCooldown(player: Player, name: String, time: String) {
        val kit = kitService.getAllKits().find { it.name.equals(name, ignoreCase = true) }
        var suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""

        if (kit == null) {
            player.sendErrorMessage("No kit found with the name '$name'.")
            return
        }

        try {
            kitService.setCooldown(name, time)
            player.sendSuccessMessage("Set cooldown for kit '$name' to $time.")
            Logger.log(suffix, player, Perm.LOG, "set the kit cooldown for kit: '$name'. To $time", false)
        } catch (e: Exception) {
            player.sendErrorMessage("Invalid time format! Use formats like 10s, 5m, 2h, or 1d.")
        }
    }

    @Subcommand("setitems")
    @CommandPermission(Perm.KIT_ADMIN)
    @CommandCompletion("@kits")
    @Syntax("<kit>")
    fun onSetKitItems(player: Player, name: String) {
        var suffix = chatService?.getPlayerSuffix(player)?.translate() ?: ""
        val kit = kitService.getAllKits().find { it.name.equals(name, ignoreCase = true) }

        if (kit == null) {
            player.sendErrorMessage("No kit found with the name '$name'.")
            return
        }

        kitService.setKitItems(name, player)
        player.sendSuccessMessage("You have updated the items for kit '$name'.")
        Logger.log(suffix, player, Perm.LOG, "updated the kit items for the kit '$name'", false)
    }

    @Subcommand("reload")
    @CommandPermission(Perm.ADMIN)
    fun reload(sender: CommandSender) {
        try {
            kitService.reloadKits()
            sender.sendMessage("You have reloaded all kits.".blue())
            Logger.info("Kits reloaded by ${sender.name}")
        } catch (ex: Exception) {
            sender.sendMessage("Failed to reload kits: ${ex.message}".red())
            ex.printStackTrace()
        }
    }
}
