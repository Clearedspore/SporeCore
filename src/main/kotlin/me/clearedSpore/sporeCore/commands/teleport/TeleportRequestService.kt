package me.clearedSpore.sporeCore.commands.teleport

import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.menu.confirm.TPAConfirmMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.user.settings.Setting
import me.clearedSpore.sporeCore.util.TeleportService.awaitTeleport
import org.bukkit.entity.Player

object TeleportRequestService {

    enum class RequestType {
        TPA, TPAHERE
    }

    data class Request(val requester: Player, val target: Player, val type: RequestType)

    private val pendingRequests = mutableMapOf<Player, Request>()

    fun sendRequest(requester: Player, target: Player, type: RequestType) {
        if (requester == target) {
            requester.sendErrorMessage("You cannot send a request to yourself.")
            return
        }

        val targetUser = UserManager.get(target)

        if(targetUser == null){
            requester.userJoinFail()
            return
        }

        if (!targetUser.isSettingEnabled(Setting.TELEPORT_REQUESTS)) {
            requester.sendErrorMessage("That player has teleport requests disabled!".red())
            return
        }

        val executeRequest = {
            pendingRequests[target] = Request(requester, target, type)
            requester.sendSuccessMessage("Teleport request sent to ${target.name}.")
            target.sendSuccessMessage(
                when (type) {
                    RequestType.TPA -> "${requester.name} wants to teleport to you. Use /tpaaccept or /tpadeny."
                    RequestType.TPAHERE -> "${requester.name} wants you to teleport to them. Use /tpaaccept or /tpadeny."
                }
            )
        }

        if (targetUser.isSettingEnabled(Setting.CONFIRM_TPA)) {
            TPAConfirmMenu(target, requester, executeRequest).open(target)
        } else {
            executeRequest()
        }
    }

    fun accept(target: Player) {
        val request = pendingRequests[target]
        if (request == null) {
            target.sendErrorMessage("You have no pending teleport requests.")
            return
        }

        val targetUser = UserManager.get(target)

        if(targetUser == null){
            request.requester.userJoinFail()
            return
        }

        val executeTeleport = {
            pendingRequests.remove(target)

            when (request.type) {
                RequestType.TPA -> {
                    request.requester.awaitTeleport(target.location)
                    request.requester.sendSuccessMessage("${target.name} accepted your teleport request.")
                    target.sendSuccessMessage("You accepted ${request.requester.name}'s teleport request.")
                }

                RequestType.TPAHERE -> {
                    target.awaitTeleport(request.requester.location)
                    target.sendSuccessMessage("You accepted ${request.requester.name}'s teleport request.")
                    request.requester.sendSuccessMessage("${target.name} accepted your teleport request.")
                }
            }
        }

        if (targetUser.isSettingEnabled(Setting.CONFIRM_TPA)) {
            TPAConfirmMenu(target, request.requester, executeTeleport).open(target)
        } else {
            executeTeleport()
        }
    }

    fun deny(target: Player) {
        val request = pendingRequests.remove(target)
        if (request == null) {
            target.sendErrorMessage("You have no pending teleport requests.")
            return
        }

        request.requester.sendErrorMessage("Your teleport request to ${request.target.name} was denied.")
        target.sendSuccessMessage("You denied ${request.requester.name}'s teleport request.")
    }
}
