package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuid
import me.clearedSpore.sporeCore.features.message.Message
import me.clearedSpore.sporeCore.features.message.MessageType
import me.clearedSpore.sporeCore.menu.user.messages.MessagesMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("messages|mail|checkmessages")
@SporeCoreCommand
class MessagesCommand : BaseCommand() {

    @Default()
    fun onMessages(player: Player) {
        val user = UserManager.get(player)

        if (user == null) {
            player.userFail()
            return
        }

        if (user.messages.isEmpty()) {
            player.sendErrorMessage("You don't have any messages!")
            return
        }

        MessagesMenu(user, player).open(player)

    }

    @Subcommand("admin send")
    @CommandPermission(Perm.MESSAGE_ADMIN)
    @CommandCompletion("@players")
    fun onAdd(player: CommandSender, targetName: String, type: MessageType, text: String) {
        val user = UserManager.get(targetName)

        if (user == null) {
            player.userJoinFail()
            return
        }

        val message = Message(
            UUID.randomUUID().toString(),
            System.currentTimeMillis(),
            type,
            text,
            player.uuid(),
            true
        )

        user.queueMessage(message)
        player.sendSuccessMessage("Successfully send the message to &f${user.playerName}")
    }

    @Subcommand("admin view")
    @CommandPermission(Perm.MESSAGE_ADMIN)
    @CommandCompletion("@players")
    fun onView(player: Player, targetName: String) {
        val user = UserManager.get(targetName)

        if (user == null) {
            player.userJoinFail()
            return
        }

        if(user.messages.isEmpty()) {
            player.sendErrorMessage("That player does not have any messages!")
            return
        }

        MessagesMenu(user, player, true).open(player)

    }

}