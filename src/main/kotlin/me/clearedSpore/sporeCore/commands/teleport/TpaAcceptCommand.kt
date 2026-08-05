package me.clearedSpore.sporeCore.commands.teleport

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import org.bukkit.entity.Player

@CommandAlias("tpaccept")
class TpaAcceptCommand() : BaseCommand() {

    @Default
    fun onAccept(player: Player) {
        TeleportRequestService.accept(player)
    }
}

@CommandAlias("tpdeny")
class TpaDenyCommand : BaseCommand() {

    @Default
    fun onDeny(player: Player) {
        TeleportRequestService.deny(player)
    }
}
