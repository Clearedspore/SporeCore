package me.clearedSpore.sporeCore.features.investigation

import me.clearedSpore.sporeAPI.exception.LoggedException
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.hasJoinedBefore
import me.clearedSpore.sporeCore.extension.PlayerExtension.safeUuidStr
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuid
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.IGLogType
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationPriority
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationStatus
import me.clearedSpore.sporeCore.features.investigation.`object`.log.IGLog
import me.clearedSpore.sporeCore.features.investigation.`object`.note.Note
import me.clearedSpore.sporeCore.features.investigation.`object`.suspect.Suspect
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.reports.ReportService
import me.clearedSpore.sporeCore.features.reports.`object`.Report
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.dizitart.no2.filters.FluentFilter.where
import java.util.UUID
import kotlin.uuid.Uuid

object IGService {

    internal val igCollection get() = DatabaseManager.getInvestigationCollection()

    fun startInvestigation(creator: Player, name: String, description: String, priority: InvestigationPriority) {

        val investigation = Investigation(
            UUID.randomUUID().toString(),
            name,
            creator.uniqueId.toString(),
            description,
            System.currentTimeMillis(),
            notes = mutableListOf(),
            linkedReports = mutableListOf(),
            linkedPunishments = mutableListOf(),
            suspects = mutableListOf(),
            staff = mutableListOf(),
            admin = mutableListOf(creator.uuidStr()),
            logs = mutableListOf(),
            InvestigationStatus.IN_PROGRESS,
            priority = priority
        )

        try {
            igCollection.insert(investigation.toDocument())
            Logger.infoDB("New investigation '$name' made by ${creator.name}")
            Logger.log(creator, Perm.ADMIN_LOG, "started a new investigation", false)
            logAction(investigation.id, IGLogType.STARTED, creator.safeUuidStr(), "Started the investigation")
            creator.sendSuccessMessage("Successfully started investigation")
            ManageIGMenu(investigation.id, creator).open(creator)
        } catch (ex: Exception) {
            throw LoggedException(
                userMessage = "An internal database error occurred while creating the investigation.",
                internalMessage = "Failed to insert investigation '$name' for ${creator.name}'",
                channel = LoggedException.Channel.DATABASE,
                developerOnly = true,
                cause = ex,
            ).also { it.log() }
        }

    }

    fun addReport(investigationID: String, playerID: String, reportID: String) {
        val config = SporeCore.instance.coreConfig

        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            return
        }

        if (!config.features.reports) {
            Logger.errorDB("Failed to add report: Reports are disabled!")
            return
        }

        val reportDoc = ReportService.reportCollection.find(where("id").eq(reportID)).firstOrNull()
        val report = reportDoc?.let { Report.fromDocument(it) }

        if (report != null) {
            investigation.linkedReports.add(report)

            try {
                updateInvestigation(investigation)
                Logger.infoDB("Added report '${report.id}' to investigation '${investigation.name}'")
                logAction(investigation.id, IGLogType.REPORT, playerID, "Added report ${report.id} to the investigation")
            } catch (ex: Exception) {
                throw LoggedException(
                    userMessage = "Failed to add the report to the investigation.",
                    internalMessage = "Error while adding report '${report.id}' to '${investigation.id}'",
                    channel = LoggedException.Channel.DATABASE,
                    developerOnly = true,
                    cause = ex
                ).also { it.log() }
            }
        } else {
            Logger.errorDB("Report with ID $reportID not found")
        }
    }

    fun removeReport(investigationID: String, playerID: String, reportID: String) {
        val config = SporeCore.instance.coreConfig

        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            return
        }

        if (!config.features.reports) {
            Logger.errorDB("Failed to add report: Reports are disabled!")
            return
        }

        val report = investigation.linkedReports.find { it.id == reportID }

        if (report != null) {
            investigation.linkedReports.remove(report)

            try {
                updateInvestigation(investigation)
                Logger.infoDB("Removed report '${report.id}' from investigation '${investigation.name}'")
                logAction(investigation.id, IGLogType.REPORT, playerID, "Removed report ${report.id} from the investigation")
            } catch (ex: Exception) {
                throw LoggedException(
                    userMessage = "Failed to remove the report from the investigation.",
                    internalMessage = "Error while removing report '${report.id}' from '${investigation.id}'",
                    channel = LoggedException.Channel.DATABASE,
                    developerOnly = true,
                    cause = ex
                ).also { it.log() }
            }
        } else {
            Logger.errorDB("Report with ID $reportID not found")
        }
    }

    fun addSuspect(investigationID: String, suspectID: String, description: String, addedBy: String) {
        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            return
        }

        val target = Bukkit.getOfflinePlayer(suspectID)
        if (!target.hasJoinedBefore() || UserManager.get(suspectID) == null) {
            Logger.errorDB("Cannot find target with ID '$suspectID' for investigation '$investigationID'")
            return
        }

        val suspect = Suspect(
            UUID.randomUUID().toString(),
            suspectID,
            description,
            addedBy,
            System.currentTimeMillis()
        )

        investigation.suspects.add(suspect)

        try {
            updateInvestigation(investigation)
            Logger.infoDB("Added suspect '$suspectID' to investigation '${investigation.name}'")
            logAction(investigation.id, IGLogType.SUSPECT, addedBy, "Added suspect ${suspect.getSuspectName()} to the investigation")
        } catch (ex: Exception) {
            throw LoggedException(
                userMessage = "Failed to add the suspect to the investigation.",
                internalMessage = "Error while adding suspect '$suspectID' to '${investigation.id}'",
                channel = LoggedException.Channel.DATABASE,
                developerOnly = true,
                cause = ex
            ).also { it.log() }
        }
    }


    fun removeSuspect(investigationID: String, suspectID: String, playerID: String) {
        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            return
        }


        val suspect = investigation.suspects.find { it.id == suspectID }

        if (suspect != null) {
            investigation.suspects.remove(suspect)

            try {
                updateInvestigation(investigation)
                Logger.infoDB("Removed suspect '$suspectID' from investigation '${investigation.name}'")
                logAction(investigation.id, IGLogType.SUSPECT, playerID, "Removed suspect ${suspect.getSuspectName()} from the investigation")
            } catch (ex: Exception) {
                throw LoggedException(
                    userMessage = "Failed to remove the suspect from the investigation.",
                    internalMessage = "Error while removing suspect '$suspectID' from '${investigation.id}'",
                    channel = LoggedException.Channel.DATABASE,
                    developerOnly = true,
                    cause = ex
                ).also { it.log() }
            }
        } else {
            Logger.errorDB("Cannot find suspect with ID '$suspectID' for investigation '$investigationID'")
        }
    }

    fun addNote(investigationID: String, name: String, text: String, addedBy: Player) {
        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            return
        }

        val note = Note(
            UUID.randomUUID().toString(),
            name,
            System.currentTimeMillis(),
            addedBy.name,
            text
        )

        investigation.notes.add(note)

        try {
            updateInvestigation(investigation)
            Logger.infoDB("Added note '${note.id}' to investigation '${investigation.name}'")
            logAction(investigation.id, IGLogType.NOTE, addedBy.safeUuidStr(), "Addded note ${note.name} to the investigation")
        } catch (ex: Exception) {
            throw LoggedException(
                userMessage = "Failed to add the note to the investigation.",
                internalMessage = "Error while adding note '${note.id}' to '${investigation.id}'",
                channel = LoggedException.Channel.DATABASE,
                developerOnly = true,
                cause = ex
            ).also { it.log() }
        }
    }


    fun removeNote(investigationID: String, noteID: String, playerID: String) {
        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            return
        }


        val note = investigation.notes.find { it.id == noteID }

        if (note != null) {
            investigation.notes.remove(note)

            try {
                updateInvestigation(investigation)
                Logger.infoDB("Removed note '$noteID' from investigation '${investigation.name}'")
                logAction(investigation.id, IGLogType.NOTE, playerID, "Removed note ${note.name} from the investigation")
            } catch (ex: Exception) {
                throw LoggedException(
                    userMessage = "Failed to remove note from the investigation.",
                    internalMessage = "Error while removing note '$noteID' from '${investigation.id}'",
                    channel = LoggedException.Channel.DATABASE,
                    developerOnly = true,
                    cause = ex
                ).also { it.log() }
            }
        } else {
            Logger.errorDB("Cannot find note with ID '$noteID' for investigation '$investigationID'")
        }
    }

    fun addPunishment(sender: Player, investigationID: String, punishID: String) {
        val config = SporeCore.instance.coreConfig

        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            sender.sendErrorMessage("Investigation not found")
            return
        }

        if (!config.features.punishments) {
            Logger.errorDB("Failed to add punishment: Punishments are disabled")
            sender.sendErrorMessage("Punishments are disabled")
            return
        }

        sender.sendMessage("Finding punishment by id. This may take a while..".blue())

        val punishment = PunishmentService.findPunishmentById(punishID) ?: run {
            Logger.errorDB("Cannot find punishment with ID $punishID")
            sender.sendErrorMessage("Cannot find punishment with ID $punishID")
            return
        }

        if (investigation.linkedPunishments.any { it.id == punishment.id }) {
            sender.sendErrorMessage("That punishment is already linked to this investigation")
            return
        }

        investigation.linkedPunishments.add(punishment)

        try {
            updateInvestigation(investigation)
            Logger.infoDB("Added punishment '$punishID' to investigation '${investigation.name}'")
            logAction(investigation.id, IGLogType.PUNISHMENT, sender.safeUuidStr(), "Added punishment ${punishment.id} to the investigation")
            sender.sendMessage("Punishment linked successfully".green())
        } catch (ex: Exception) {
            throw LoggedException(
                userMessage = "Failed to add the punishment to the investigation.",
                internalMessage = "Error while adding punishment '$punishID' to '${investigation.id}'",
                channel = LoggedException.Channel.DATABASE,
                developerOnly = true,
                cause = ex
            ).also { it.log() }
        }
    }

    fun removePunishment(sender: Player, investigationID: String, punishID: String) {
        val config = SporeCore.instance.coreConfig

        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            sender.sendErrorMessage("Investigation not found")
            return
        }

        if (!config.features.punishments) {
            Logger.errorDB("Failed to remove punishment: Punishments are disabled")
            sender.sendErrorMessage("Punishments are disabled")
            return
        }

        sender.sendMessage("Finding punishment by id. This may take a while..".blue())

        val punishment = PunishmentService.findPunishmentById(punishID) ?: run {
            Logger.errorDB("Cannot find punishment with ID $punishID")
            sender.sendErrorMessage("Cannot find punishment with ID $punishID")
            return
        }

        if (investigation.linkedPunishments.any { it.id != punishment.id }) {
            sender.sendErrorMessage("That punishment is not linked to this investigation")
            return
        }

        investigation.linkedPunishments.remove(punishment)

        try {
            updateInvestigation(investigation)
            Logger.infoDB("Removed punishment '$punishID' from investigation '${investigation.name}'")
            sender.sendMessage("Punishment removed successfully".green())
            logAction(investigation.id, IGLogType.PUNISHMENT, sender.safeUuidStr(), "Removed punishment ${punishment.id} from the investigation")
        } catch (ex: Exception) {
            throw LoggedException(
                userMessage = "Failed to remove the punishment from the investigation.",
                internalMessage = "Error while removing punishment '$punishID' from '${investigation.id}'",
                channel = LoggedException.Channel.DATABASE,
                developerOnly = true,
                cause = ex
            ).also { it.log() }
        }
    }

    fun logAction(investigationID: String, type: IGLogType, user: String, action: String) {

        val investigation = findInvestigation(investigationID) ?: run {
            Logger.errorDB("Cannot find investigation with ID $investigationID")
            return
        }

        val log = IGLog(
            UUID.randomUUID().toString(),
            user,
            System.currentTimeMillis(),
            action,
            type
        )

        investigation.logs.add(log)
        try {
            updateInvestigation(investigation)
            Logger.infoDB("Successfully logged a new action for investigation '${investigation.name}'")
        } catch (ex: Exception) {
            throw LoggedException(
                userMessage = "Failed to log action.",
                internalMessage = "Error while logging an action for investigatition '${investigation.id}'",
                channel = LoggedException.Channel.DATABASE,
                developerOnly = true,
                cause = ex
            ).also { it.log() }
        }
    }

    fun updateInvestigation(investigation: Investigation) {
        try {
            igCollection.update(
                where("id").eq(investigation.id),
                investigation.toDocument()
            )
        } catch (ex: Exception) {
            throw LoggedException(
                userMessage = "Failed to update Investigation with ID ${investigation.id}",
                internalMessage = "Error updating investigation '${investigation.id}'",
                channel = LoggedException.Channel.DATABASE,
                developerOnly = true,
                cause = ex
            ).also { it.log() }
        }
    }

    fun findInvestigation(investigationID: String): Investigation? {
        val igDoc = igCollection.find(where("id").eq(investigationID)).firstOrNull()
        return Investigation.fromDocument(igDoc)
    }

    fun isStaff(player: Player): Boolean {
        val name = player.name

        return igCollection
            .find()
            .mapNotNull { Investigation.fromDocument(it) }
            .any { investigation ->
                investigation.staff.any { it.equals(name, ignoreCase = true) }
            }
    }

    fun isAdmin(player: Player): Boolean {
        val name = player.name

        return igCollection
            .find()
            .mapNotNull { Investigation.fromDocument(it) }
            .any { investigation ->
                investigation.admin.any { it.equals(name, ignoreCase = true) }
            }
    }

}