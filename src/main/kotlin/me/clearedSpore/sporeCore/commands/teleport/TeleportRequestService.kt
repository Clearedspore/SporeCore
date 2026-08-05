package me.clearedSpore.sporeCore.commands.teleport

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.userJoinFail
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService.chatService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.setting.impl.AutoTeleportSetting
import me.clearedSpore.sporeCore.features.setting.impl.ConfirmTpaSetting
import me.clearedSpore.sporeCore.features.setting.impl.TeleportRequestSettings
import me.clearedSpore.sporeCore.menu.util.confirm.TPAConfirmMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ActionBar.actionBar
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

        val requesterUser = UserManager.get(requester)
        val targetUser = UserManager.get(target)

        if (requesterUser == null) {
            Logger.error("Failed to load user for &e${requester.name}")
            return
        }

        if (targetUser == null) {
            requester.userJoinFail()
            return
        }
        val requesterSuffix = chatService?.getPlayerSuffix(requester.player)?.translate()
        val targetSuffix = chatService?.getPlayerSuffix(target.player)?.translate()

        if (!targetUser.getSettingOrDefault(TeleportRequestSettings())) {
            requester.sendMessage("That player has teleport requests disabled!".red())
            return
        }

        val existingRequest = pendingRequests[target]
        if (existingRequest != null && existingRequest.requester == requester) {
            requester.sendMessage("You have already sent a request to $targetSuffix${target.name}".red() + "!".red())
            return
        }

        val request = Request(requester, target, type)
        pendingRequests[target] = request

        val executeRequest = {
            requester.actionBar("tpa", "&7Teleport request sent to $targetSuffix${target.name}&r&7.")
            when (type) {
                RequestType.TPA -> {
                    if (targetUser.getSettingOrDefault(AutoTeleportSetting())) {
                        accept(target)
                        target.actionBar("tpa", "&7Accepted $requesterSuffix${requester.name}&r&7's request (Auto-TP)")
                        pendingRequests.remove(target)
                    } else {
                        target.sendMessage(
                            "$requesterSuffix${requester.name}&r&7 wants to teleport to you. Use ".translate()
                                + "/tpaccept".blue()
                                + " or ".gray()
                                + "/tpdeny".blue()
                                + ".".gray())
                    }
                }

                RequestType.TPAHERE -> {
                    target.sendMessage("$requesterSuffix${requester.name}&r&7 wants you to teleport to them. Use ".translate()
                            + "/tpaccept".blue()
                            + " or ".gray()
                            + "/tpdeny".blue()
                            + ".".gray())
                }
            }
        }

        if (requesterUser.getSettingOrDefault(ConfirmTpaSetting())) {
            TPAConfirmMenu(requester, target, executeRequest as () -> Unit).open(requester)
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

        if (targetUser == null) {
            request.requester.userJoinFail()
            return
        }

        val requesterSuffix = chatService?.getPlayerSuffix(request.requester.player)?.translate()
        val targetSuffix = chatService?.getPlayerSuffix(target.player)?.translate()

        val executeTeleport = {
            pendingRequests.remove(target)

            when (request.type) {
                RequestType.TPA -> {
                    request.requester.awaitTeleport(target.location)
                    request.requester.actionBar("tpa", "$targetSuffix${target.name}&r&7 accepted your teleport request.")
                    target.actionBar("tpa", "&7Accepted $targetSuffix${request.requester.name}&r&7's teleport request.")

                    if (SporeCore.instance.coreConfig.logs.teleports) {
                        LogsService.addLog(
                            request.requester.uuidStr(),
                            "Teleported to ${target.name} (TPA request sent to ${target.name})",
                            LogType.TELEPORT
                        )

                        LogsService.addLog(
                            target.uuidStr(),
                            "${request.requester.name} teleported to you (TPA request sent by ${request.requester.name})",
                            LogType.TELEPORT
                        )
                    }
                }

                RequestType.TPAHERE -> {
                    target.awaitTeleport(request.requester.location)
                    target.actionBar("tpa", "&7Accepted $requesterSuffix${request.requester.name}&r&7's teleport request.")
                    request.requester.actionBar("tpa", "$targetSuffix${target.name}&r&7 accepted your teleport request.")

                    if (SporeCore.instance.coreConfig.logs.teleports) {
                        LogsService.addLog(
                            target.uuidStr(),
                            "Teleported to ${request.requester.name} (TpaHere request sent by ${request.requester.name})",
                            LogType.TELEPORT
                        )

                        LogsService.addLog(
                            request.requester.uuidStr(),
                            "${target.name} teleported to you (TpaHere request sent to ${target.name})",
                            LogType.TELEPORT
                        )
                    }
                }
            }
        }

        if (targetUser.getSettingOrDefault(ConfirmTpaSetting())) {
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
        val requesterSuffix = chatService?.getPlayerSuffix(request.requester.player)?.translate()
        val targetSuffix = chatService?.getPlayerSuffix(request.target.player)?.translate()

        request.requester.sendMessage("Your teleport request to $targetSuffix${request.target.name}".red() + " was denied.".red())
        target.sendMessage("You denied $requesterSuffix${request.requester.name}&r".blue() + "'s teleport request.".blue())
    }
}
