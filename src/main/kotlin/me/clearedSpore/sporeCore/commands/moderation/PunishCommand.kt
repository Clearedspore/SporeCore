package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Syntax
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.config.ReasonEntry
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.punishment.punish.PunishMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("punish|p")
@CommandPermission(Perm.PUNISH)
class PunishCommand : BaseCommand() {

    @Default
    @CommandCompletion("@players @reasons")
    @Syntax("<player> [reason]")
    fun onPunish(sender: CommandSender, targetName: String, @Optional reasonKey: String?) {

        val target = Bukkit.getOfflinePlayer(targetName)
        val targetUser = UserManager.get(target) ?: run {
            sender.sendMessage("Could not find player $targetName".red())
            return
        }


        val punisherUser = if (sender is Player) UserManager.get(sender.uniqueId) else UserManager.getConsoleUser()
        if (punisherUser == null) {
            sender.sendMessage("Could not resolve punisher user.".red())
            return
        }

        val settings = PunishmentService.config.settings


        if (!settings.selfPunish && punisherUser.uuid == targetUser.uuid) {
            sender.sendMessage("You cannot punish yourself.".red())
            return
        }


        val bypassPerm = settings.permBypass
        if (!bypassPerm.isNullOrBlank() && targetUser.player?.hasPermission(bypassPerm) == true) {
            sender.sendMessage("You cannot punish this player.".red())
            return
        }


        if (reasonKey == null) {
            if (sender is Player) {
                PunishMenu(sender, target).open(sender)
            } else {
                sender.sendMessage("Console must provide a reason!".red())
            }
            return
        }


        val reasonPair = PunishmentService.findReasonDefinition(reasonKey)
        val reasonDefinition = reasonPair?.second
        val isCustomReason = reasonDefinition == null

        if (settings.requireReason && isCustomReason) {
            sender.sendMessage("You must provide a valid reason.".red())
            return
        }

        val reasonKeyLower = reasonKey.lowercase()
        val pastOffenses = targetUser.punishments.count { it.offense.equals(reasonKeyLower, ignoreCase = true) }
        val nextOffense = pastOffenses + 1


        val offenseEntry = reasonDefinition?.let {
            it.offenses[nextOffense] ?: it.offenses[it.offenses.keys.maxOrNull()!!]
        } ?: ReasonEntry(PunishmentType.WARN, reasonKey, null)


        val typePerm = "sporecore.punishments.${offenseEntry.type.name.lowercase()}"
        if (!sender.hasPermission(typePerm)) {
            sender.sendMessage("You do not have permission to ${offenseEntry.type.displayName.lowercase()} players.".red())
            return
        }

        PunishmentService.punish(
            targetUser = targetUser,
            punisher = punisherUser,
            rawReason = offenseEntry.reason,
            providedType = offenseEntry.type,
            providedTime = offenseEntry.time
        )


    }
}


@CommandAlias("ban")
@CommandPermission(Perm.BAN)
class BanCmd : AbstractPunishCommand(PunishmentType.BAN, requiresTime = false) {
    override val customPermission = "sporecore.punishments.ban.custom"

    @Default
    @CommandCompletion("@players @reasons")
    @Syntax("<player> <reason>")
    fun execute(sender: CommandSender, targetName: String, @Optional reason: String?) {
        handle(sender, targetName, null, reason)
    }
}

@CommandAlias("tempban")
@CommandPermission(Perm.TEMP_BAN)
class TempBanCmd : AbstractPunishCommand(PunishmentType.TEMPBAN, requiresTime = true) {
    override val customPermission = "sporecore.punishments.tempban.custom"

    @Default
    @CommandCompletion("@players @times @reasons")
    @Syntax("<player> <time> <reason>")
    fun execute(sender: CommandSender, targetName: String, @Optional time: String?, @Optional reason: String?) {
        handle(sender, targetName, time, reason)
    }
}

@CommandAlias("mute")
@CommandPermission(Perm.MUTE)
class MuteCmd : AbstractPunishCommand(PunishmentType.MUTE, requiresTime = false) {
    override val customPermission = "sporecore.punishments.mute.custom"

    @Default
    @CommandCompletion("@players @reasons")
    @Syntax("<player> <reason>")
    fun execute(sender: CommandSender, targetName: String, @Optional reason: String?) {
        handle(sender, targetName, null, reason)
    }
}

@CommandAlias("tempmute")
@CommandPermission(Perm.TEMP_MUTE)
class TempMuteCmd : AbstractPunishCommand(PunishmentType.TEMPMUTE, requiresTime = true) {
    override val customPermission = "sporecore.punishments.tempmute.custom"

    @Default
    @CommandCompletion("@players @times @reasons")
    @Syntax("<player> <time> <reason>")
    fun execute(sender: CommandSender, targetName: String, @Optional time: String?, @Optional reason: String?) {
        handle(sender, targetName, time, reason)
    }
}

@CommandAlias("kick")
@CommandPermission(Perm.KICK)
class KickCmd : AbstractPunishCommand(PunishmentType.KICK, requiresTime = false) {
    override val customPermission = "sporecore.punishments.kick.custom"

    @Default
    @CommandCompletion("@players @reasons")
    @Syntax("<player> <reason>")
    fun execute(sender: CommandSender, targetName: String, @Optional reason: String?) {
        handle(sender, targetName, null, reason)
    }
}

@CommandAlias("warn")
@CommandPermission(Perm.WARN)
class WarnCmd : AbstractPunishCommand(PunishmentType.WARN, requiresTime = false) {
    override val customPermission = "sporecore.punishments.warn.custom"

    @Default
    @CommandCompletion("@players @reasons")
    @Syntax("<player> <reason>")
    fun execute(sender: CommandSender, targetName: String, @Optional reason: String?) {
        handle(sender, targetName, null, reason)
    }
}

@CommandAlias("tempwarn")
@CommandPermission(Perm.TEMP_WARN)
class TempWarnCmd : AbstractPunishCommand(PunishmentType.TEMPWARN, requiresTime = true) {
    override val customPermission = "sporecore.punishments.tempwarn.custom"

    @Default
    @CommandCompletion("@players @times @reasons")
    @Syntax("<player> <time> <reason>")
    fun execute(sender: CommandSender, targetName: String, @Optional time: String?, @Optional reason: String?) {
        handle(sender, targetName, time, reason)
    }
}
