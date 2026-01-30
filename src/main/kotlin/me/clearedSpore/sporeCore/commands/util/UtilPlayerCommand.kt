package me.clearedSpore.sporeCore.commands.util

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.StringUtil.joinWithSpaces
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.button.TextButton
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta


@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@SporeCoreCommand
class UtilPlayerCommand : BaseCommand() {

    @Subcommand("player uuid")
    @CommandPermission(Perm.UTIL_PLAYER)
    @CommandCompletion("@players")
    fun getUUID(sender: CommandSender, @Name("player") targetName: String) {
        val target = Bukkit.getOfflinePlayer(targetName)

        if (!target.hasPlayedBefore()) {
            sender.userJoinFail()
            return
        }

        sender.sendSuccessMessage("$targetName UUID is: &f${target.uniqueId}")
        val button = TextButton("[Click to copy]".blue())
            .copyToClipboard(target.uniqueId.toString())
            .hoverEvent("Click to copy!")
            .onClick {
                sender.sendSuccessMessage("Successfully copied!")
            }
            .build(sender)
        sender.sendMessage(button)
    }

    @Subcommand("player skull")
    @CommandPermission(Perm.UTIL_PLAYER)
    @CommandCompletion("@players")
    fun skull(sender: CommandSender, @Name("player") targetName: String) {
        val target: OfflinePlayer = Bukkit.getOfflinePlayer(targetName)
        if (!target.hasPlayedBefore() && !target.isOnline) {
            sender.userJoinFail()
            return
        }

        val head = ItemStack(org.bukkit.Material.PLAYER_HEAD)
        val meta = head.itemMeta as SkullMeta
        meta.owningPlayer = target
        head.itemMeta = meta

        sender.sendSuccessMessage("Player skull for ${target.name} created!")
        if (sender is Player) {
            sender.inventory.addItem(head)
        }
    }

    @Subcommand("player sendmessage")
    @CommandPermission(Perm.UTIL_PLAYER)
    @CommandCompletion("@players")
    fun onSendMessage(
        sender: CommandSender,
        @Name("player") targetOnline: OnlinePlayer,
        @Name("message") messageParts: String
    ) {
        val target = targetOnline.player
        val message = messageParts.joinWithSpaces()

        target.sendMessage(message.translate())
        sender.sendSuccessMessage("Successfully send the message to ${target.name}")
        sender.sendMessage("Message: $message".blue())
    }

    @Subcommand("player skull id")
    @CommandPermission(Perm.UTIL_PLAYER)
    @Syntax("<id>")
    fun skullId(sender: CommandSender, skullId: String) {
        val head = ItemStack(org.bukkit.Material.PLAYER_HEAD)
        val meta = head.itemMeta as SkullMeta
        meta.ownerProfile = Bukkit.getOfflinePlayer(skullId).playerProfile
        head.itemMeta = meta


        sender.sendSuccessMessage("Custom skull with ID $skullId created!")
        if (sender is Player) {
            sender.inventory.addItem(head)
        }
    }


    @Subcommand("player vanished")
    @CommandPermission(Perm.UTIL_PLAYER)
    @CommandCompletion("@players")
    fun vanished(sender: CommandSender, @Name("player") targetName: OnlinePlayer) {
        val target: OfflinePlayer = targetName.player

        val isVanished = VanishService.isVanished(target.uniqueId)
        if (isVanished) {
            sender.sendSuccessMessage("${target.name} is &fvanished!")
        } else {
            sender.sendSuccessMessage("${target.name} is &fvisible!")
        }
    }

    @Subcommand("player location")
    @CommandPermission(Perm.UTIL_PLAYER)
    @CommandCompletion("@players")
    fun location(sender: CommandSender, @Name("player") targetName: String) {
        val target = Bukkit.getPlayer(targetName)
        if (target == null) {
            sender.sendErrorMessage("$targetName is offline, cannot retrieve location!")
            return
        }

        val loc = target.location
        val copyString = "${loc.blockX}, ${loc.blockY}, ${loc.blockZ}, ${loc.world?.name ?: "world"}"
        sender.sendSuccessMessage("${target.name}'s location: ${loc.world?.name} x:${loc.blockX} y:${loc.blockY} z:${loc.blockZ}")

        if (sender is Player) {
            val copyButton = TextButton("[Copy]".blue())
                .copyToClipboard(copyString)
                .hoverEvent("Copy coordinates")
                .build(sender)

            val components = mutableListOf(copyButton)

            if (sender.hasPermission(Perm.TELEPORT_CORDS)) {
                val teleportButton = TextButton(" [Teleport]".blue())
                    .runCommand("/tppos ${loc.blockX} ${loc.blockY} ${loc.blockZ}")
                    .hoverEvent("Click to teleport!")
                    .build(sender)
                components.add(teleportButton)
            }

            val finalComponent = Component.join(JoinConfiguration.separator(Component.text(" ")), components)
            sender.sendMessage(finalComponent)
        }
    }
}