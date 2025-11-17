package me.clearedSpore.sporeCore.features.punishment

import com.github.benmanes.caffeine.cache.Caffeine
import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.config.PunishmentConfig
import me.clearedSpore.sporeCore.features.punishment.config.ReasonDefinition
import me.clearedSpore.sporeCore.features.punishment.config.ReasonEntry
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.listener.PunishmentListener
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object PunishmentService {

    lateinit var config: PunishmentConfig
        private set

    var loaded = false

    private val recentPunishments = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build<UUID, Boolean>()

    fun load() {
        loadConfig()

        Bukkit.getPluginManager().registerEvents(PunishmentListener(), SporeCore.instance)
    }

    private fun loadConfig(): PunishmentConfig {
        val dataFolder = SporeCore.instance.dataFolder
        val configFile = File(dataFolder, "punishments.yml").toPath()
        return try {
            config = YamlConfigurations.update(configFile, PunishmentConfig::class.java)
            Logger.info("Loaded punishments.yml successfully.")
            loaded = true
            config
        } catch (ex: ConfigurationException) {
            Logger.error("Invalid config detected — defaults applied.")
            ex.printStackTrace()
            config = PunishmentConfig()
            loaded = false
            config
        }
    }

    fun getReasonByName(category: String, reasonName: String): ReasonDefinition? {
        if (!loaded) return null
        return config.reasons.categories[category.lowercase()]?.get(reasonName.lowercase())
    }

    fun getAllReasons(): Map<String, Map<String, ReasonDefinition>> {
        if (!loaded) return emptyMap()
        return config.reasons.categories
    }

    fun getCategories(): Set<String> {
        if (!loaded) return emptySet()
        return config.reasons.categories.keys
    }

    fun getReasonsInCategory(category: String): Map<String, ReasonDefinition> {
        if (!loaded) return emptyMap()
        return config.reasons.categories[category.lowercase()] ?: emptyMap()
    }


    private fun Collection<MutableMap<String, ReasonDefinition>>.flattenToMap(): Map<String, ReasonDefinition> {
        val map = mutableMapOf<String, ReasonDefinition>()
        forEach { map.putAll(it) }
        return map
    }

    fun findReasonDefinition(reasonKey: String): Pair<String, ReasonDefinition>? {
        val lower = reasonKey.lowercase()
        for ((category, reasons) in config.reasons.categories) {
            val found = reasons[lower]
            if (found != null) return category to found
        }
        return null
    }



    fun punish(
        targetUser: User,
        punisher: User,
        rawReason: String,
        providedType: PunishmentType?,
        providedTime: String?
    ) {
        if (recentPunishments.getIfPresent(targetUser.uuid) != null) {
            punisher.player?.sendMessage("That player was recently punished!".red())
            return
        }

        recentPunishments.put(targetUser.uuid, true)

        val reasonKey = rawReason.lowercase()
        val categories = config.reasons.categories

        val foundReasonDef = categories
            .flatMap { (_, reasons) -> reasons.entries }
            .firstOrNull { (reasonName, _) -> reasonName.equals(reasonKey, ignoreCase = true) }
            ?.value


        val (type, reason, time, offenseKey) = if (foundReasonDef != null) {
            val pastOffenses = targetUser.punishments.count { it.offense.equals(reasonKey, ignoreCase = true) }
            val nextOffense = pastOffenses + 1

            val offenseConfig = foundReasonDef.offenses[nextOffense]
                ?: foundReasonDef.offenses[foundReasonDef.offenses.keys.maxOrNull()]!! // use highest if exceeded

            Quad(offenseConfig.type, offenseConfig.reason, offenseConfig.time, reasonKey)
        } else {
            Quad(
                providedType ?: PunishmentType.WARN,
                rawReason,
                providedTime,
                reasonKey
            )
        }

        if(targetUser.getActivePunishment(type) != null){
            punisher.sendMessage("That player is already punished!".red())
            return
        }

        val now = Date()
        val expireDate = time?.takeIf { it.isNotBlank() }?.let {
            Date(now.time + TimeUtil.parseDuration(it))
        }

        val punishment = Punishment(
            type = type,
            userUuid = targetUser.uuid,
            punisherUuid = punisher.uuid,
            expireDate = expireDate,
            punishDate = now,
            reason = reason,
            offense = offenseKey
        )

        targetUser.punishments.add(punishment)
        UserManager.save(targetUser)
        logPunishment(punishment)
        punisher.sendMessage("Successfully punished ${targetUser.playerName.blue()} for $reason.".blue())

        when (type) {
            PunishmentType.BAN,
            PunishmentType.TEMPBAN,
            PunishmentType.KICK -> handleKickPunishment(targetUser, punisher, punishment)

            PunishmentType.MUTE,
            PunishmentType.TEMPMUTE,
            PunishmentType.WARN,
            PunishmentType.TEMPWARN -> handleChatPunishment(targetUser, punisher, punishment)
        }
    }




    private fun handleKickPunishment(target: User, punisher: User, punishment: Punishment) {
        val msg = getMessage(punishment.type)
        target.kick(buildMessage(msg, punishment))
        Logger.info("&f${target.playerName} &cwas ${punishment.type.displayName.lowercase()} by &f${punisher.playerName} &cfor &e${punishment.reason}".translate())
    }

    private fun handleChatPunishment(target: User, punisher: User, punishment: Punishment) {
        val msg = getMessage(punishment.type)
        target.player?.sendMessage(buildMessage(msg, punishment))
        Logger.info("&f${target.playerName} &cwas ${punishment.type.displayName.lowercase()} by &f${punisher.playerName} &cfor &e${punishment.reason}".translate())
    }

    fun getMessage(type: PunishmentType): List<String> {
        val messages = config.messages
        return when (type) {
            PunishmentType.BAN -> messages.ban
            PunishmentType.TEMPBAN -> messages.tempBan
            PunishmentType.MUTE -> messages.mute
            PunishmentType.TEMPMUTE -> messages.tempMute
            PunishmentType.KICK -> messages.kick
            PunishmentType.WARN -> messages.warn
            PunishmentType.TEMPWARN -> messages.tempWarn
        }
    }

    fun buildMessage(lines: List<String>, punishment: Punishment): String {
        val timeLeft = punishment.getDurationFormatted()
        return lines.joinToString("\n") {
            it.replace("%reason%", punishment.reason)
                .replace("%punisher%", punishment.getPunisher()?.playerName ?: "Unknown")
                .replace("%time%", timeLeft)
                .replace("%date%", punishment.punishDate.toString())
                .replace("%id%", punishment.id.toString())
        }.translate()
    }

    fun buildTryMessage(template: String, punishment: Punishment, user: User): String {
        val timeLeft = punishment.getDurationFormatted()
        return template
            .replace("%user%", user.playerName)
            .replace("%reason%", punishment.reason)
            .replace("%time%", timeLeft)
            .translate()
    }

    fun buildRemovalMessage(template: String, punishment: Punishment, user: User, senderUser: User): String {
        val timeLeft = punishment.getDurationFormatted()
        return template
            .replace("%target%", user.playerName)
            .replace("%user%",  senderUser.playerName)
            .replace("%reason%", punishment.removalReason.toString())
            .replace("%time%", timeLeft)
            .translate()
    }

    fun buildAltEvasionScreen(user: User, altPunishment: Punishment): List<String> {
        val timeLeft = altPunishment.getDurationFormatted()
        val punisherName = altPunishment.getPunisher()?.playerName ?: "Unknown"
        val date = altPunishment.punishDate.toString()
        val id = altPunishment.id.toString()
        val altName = altPunishment.getUser()?.playerName ?: "Unknown"

        return config.messages.evasion.map { line ->
            line.replace("%alt%", altName)
                .replace("%reason%", altPunishment.reason)
                .replace("%punisher%", punisherName)
                .replace("%time%", timeLeft)
                .replace("%date%", date)
                .replace("%id%", id)
                .translate()
        }
    }

    fun buildAltTryMessage(user: User, altPunishment: Punishment): String {
        val timeLeft = altPunishment.getDurationFormatted()
        val altName = altPunishment.getUser()?.playerName ?: "Unknown"

        return config.alts.tryMessage
            .replace("%user%", user.playerName)
            .replace("%alt%", altName)
            .replace("%reason%", altPunishment.reason)
            .replace("%time%", timeLeft)
            .translate()
    }

    fun logPunishment(punishment: Punishment) {
        val logConfig = config.logs
        val format: String = when (punishment.type) {
            PunishmentType.BAN -> logConfig.ban
            PunishmentType.TEMPBAN -> logConfig.tempBan
            PunishmentType.KICK -> logConfig.kick
            PunishmentType.MUTE -> logConfig.mute
            PunishmentType.TEMPMUTE -> logConfig.tempMute
            PunishmentType.WARN -> logConfig.warn
            PunishmentType.TEMPWARN -> logConfig.tempWarn
        }

        val timeFormatted = punishment.getDurationFormatted()
        val message = format
            .replace("%user%", punishment.getPunisher()?.playerName ?: "Unknown")
            .replace("%action%", punishment.type.pastTense)
            .replace("%target%", punishment.getUser()?.playerName ?: "Unknown")
            .replace("%reason%", punishment.reason)
            .replace("%time%", timeFormatted)

        Message.broadcastMessageWithPermission(message.translate(), Perm.PUNISH_LOG)
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
