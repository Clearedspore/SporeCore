package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Name
import co.aikar.commands.annotation.Private
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.IGService.igCollection
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationPriority
import me.clearedSpore.sporeCore.menu.investigation.list.InvestigationListMenu
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.util.ChatInput
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.entity.Player
import org.dizitart.no2.filters.FluentFilter.where


@CommandAlias("investigation")
@CommandPermission(Perm.INVESTIGATION_STAFF)
class InvestigationCommand : BaseCommand() {

    val service = IGService

    @Default
    fun onMenu(sender: Player) {
        InvestigationListMenu(sender).open(sender)
    }

    @Subcommand("start")
    @CommandPermission(Perm.INVESTIGATION_ADMIN)
    fun onStart(
        player: Player,
        @Name("name") name: String,
        @Name("priority") priority: InvestigationPriority,
    ) {

        if (igCollection.find(where("name").eq(name)).firstOrNull() != null) {
            player.sendErrorMessage("An investigation with that name already exists.")
            return
        }


        player.sendSuccessMessage("Please provide the description of the investigation.")
        ChatInputService.begin(player, true) { input ->
            service.startInvestigation(player, name, input, priority)
        }
    }

    @Private
    @Subcommand("add punishment")
    fun onAddPunishment(sender: Player, investigationID: String, id: String) {
        IGService.addPunishment(sender, investigationID, id).let {
            sender.sendSuccessMessage("Added the investigation punishment")
        }
    }

    @Private
    @Subcommand("add report")
    fun onAddReport(sender: Player, investigationID: String, id: String) {
        IGService.addReport(investigationID, sender.uuidStr(), id).let {
            sender.sendSuccessMessage("Added the investigation report")
        }
    }

    @Private
    @Subcommand("add note")
    fun onAddNote(sender: Player, investigationID: String, name: String) {
        ChatInputService.begin(sender) { input ->
            IGService.addNote(investigationID, name, input, sender).let {
                sender.sendSuccessMessage("Added the investigation note")
            }
        }
    }

    @Private
    @Subcommand("add suspect")
    fun onAddSuspect(sender: Player, investigationID: String, name: String) {
        ChatInputService.begin(sender) { input ->
            IGService.addSuspect(investigationID, name, input, sender.name).let {
                sender.sendSuccessMessage("Added the suspect successfully")
            }
        }
    }
}