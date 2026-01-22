package me.clearedSpore.sporeCore.features.punishment

import com.github.benmanes.caffeine.cache.Caffeine
import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.punishment.config.PunishmentConfig
import me.clearedSpore.sporeCore.features.punishment.config.ReasonDefinition
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.punishment.`object`.StaffPunishmentStats
import me.clearedSpore.sporeCore.features.setting.impl.PunishmentLogsSetting
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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

    fun logToDiscord(punishment: Punishment) {
        val coreConfig = SporeCore.instance.coreConfig
        val discordConfig = coreConfig.discord

        if (!discordConfig.enabled || discordConfig.punishment.isNullOrBlank()) return

        if (discordConfig.enabled && !SporeCore.instance.discordEnabled) {
            Logger.error("Failed to load discord: Discord config enabled but the service is not initialized!")
            return
        }


        val staffUuid = punishment.punisherUuid
        val staffOffline = Bukkit.getOfflinePlayer(staffUuid)
        val target = Bukkit.getOfflinePlayer(punishment.userUuid)

        val reason = punishment.reason
        val type = punishment.type

        val staffName = staffOffline.name ?: "Console"
        val targetName = target.name ?: "Unknown"

        val skinURL = DiscordService.getAvatarURL(target.uniqueId)

        val issuer = punishment.getPunisher() ?: UserManager.getConsoleUser()
        val issuerSkinURL =
            issuer.player?.let { DiscordService.getAvatarURL(issuer.uuid) } ?: "https://mc-heads.net/avatar/Console/100"
        val linkedStaffId = issuer.discordID

        if (config.discord.requireLinked && linkedStaffId == null && issuer.player != null) {
            Logger.error("$staffName has not linked their Discord account!")
            issuer.player?.sendMessage("You must have your Discord account linked!".red())
            issuer.player?.sendMessage("Use /link to link it.".red())
            return
        }

        try {
            issuer.sendMessage("Punishing player...".blue())
            val webhook = Webhook(discordConfig.punishment)

            if (discordConfig.pingStaff && linkedStaffId != null) {
                webhook.setMessage("<@$linkedStaffId>")
            }

            val embed = Webhook.Embed()
                .setTitle("Punishment Issued")
                .setColor(getPunishmentColor(type))
                .setThumbnail(skinURL)
                .setAuthor(staffName, issuerSkinURL)
                .addField("Punisher", staffName, true)
                .addField("Target", targetName, true)
                .addField("Type", type.displayName ?: "Unknown", true)
                .addField("Reason", reason, false)

            webhook.addEmbed(embed)
            webhook.setUsername(staffName)
            webhook.setProfileURL(issuerSkinURL)
            webhook.send()
        } catch (ex: Exception) {
            Logger.error("Failed to send punishment webhook: ${ex.message}")
        }
    }


    private fun Collection<MutableMap<String, ReasonDefinition>>.flattenToMap(): Map<String, ReasonDefinition> {
        val map = mutableMapOf<String, ReasonDefinition>()
        forEach { map.putAll(it) }
        return map
    }

    fun findPunishmentById(punishmentId: String): Punishment? {
        return UserManager.getAllStoredUUIDsFromDB()
            .asSequence()
            .mapNotNull { UserManager.get(it) }
            .flatMap { it.punishments.asSequence() }
            .firstOrNull { it.id == punishmentId }
    }


    fun findReasonDefinition(reasonKey: String): Pair<String, ReasonDefinition>? {
        val lower = reasonKey.lowercase()
        for ((category, reasons) in config.reasons.categories) {
            val found = reasons[lower]
            if (found != null) return category to found
        }
        return null
    }

    private fun getPunishmentColor(type: PunishmentType): Int {
        return when (type) {
            PunishmentType.TEMPBAN,
            PunishmentType.BAN -> 0xFF0000

            PunishmentType.WARN,
            PunishmentType.TEMPWARN -> 0xFFA500

            PunishmentType.MUTE,
            PunishmentType.TEMPMUTE -> 0x0000FF

            PunishmentType.KICK -> 0xFFFF00

            else -> 0x000000
        }
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

        val linkedDCID = punisher.discordID

        if (linkedDCID == null && punisher is Player) {
            punisher.sendMessage("You must have your account linked before you can punish".red())
            punisher.sendMessage("Run /link to link your account!".blue())
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

        when (type) {
            PunishmentType.BAN, PunishmentType.TEMPBAN -> {
                if (targetUser.getActivePunishment(PunishmentType.BAN) != null) {
                    punisher.sendMessage("That user is already banned!".red())
                    return
                }
            }

            PunishmentType.MUTE, PunishmentType.TEMPMUTE -> {
                if (targetUser.getActivePunishment(PunishmentType.MUTE) != null) {
                    punisher.sendMessage("That player is already muted!".red())
                    return
                }
            }

            else -> {}
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

        punisher.staffStats.add(
            StaffPunishmentStats(
                targetUuid = targetUser.uuid,
                type = type,
                date = now,
                punishmentId = punishment.id,
                reason = reason
            )
        )

        targetUser.punishments.add(punishment)
        UserManager.updateCache(targetUser)

        if (type == PunishmentType.BAN || type == PunishmentType.TEMPBAN || type == PunishmentType.KICK) {
            targetUser.save(UserManager.userCollection, silent = false)
        } else {
            UserManager.save(targetUser)
        }


        if (targetUser != punisher) {
            UserManager.save(punisher)
        }

        logToDiscord(punishment)
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

    fun removePunishment(
        target: User,
        sender: User,
        punishmentId: String,
        reason: String
    ): Boolean {
        val punishment = target.punishments.firstOrNull { it.id == punishmentId } ?: return false

        punishment.removalUserUuid = sender.uuid
        punishment.removalReason = reason
        punishment.removalDate = Date()

        UserManager.save(target)
        return true
    }

    fun removePunishment(
        punishment: Punishment,
        senderUser: User,
        targetUser: User
    ): Boolean {
        return when (punishment.type) {
            PunishmentType.BAN, PunishmentType.TEMPBAN ->
                targetUser.unban(senderUser, punishment.id, "Rollback")

            PunishmentType.MUTE, PunishmentType.TEMPMUTE ->
                targetUser.unmute(senderUser, punishment.id, "Rollback")

            PunishmentType.WARN, PunishmentType.TEMPWARN ->
                targetUser.unwarn(senderUser, punishment.id, "Rollback")

            else -> false
        }
    }


    private fun handleKickPunishment(target: User, punisher: User, punishment: Punishment) {
        val player = Bukkit.getPlayer(target.uuid) ?: run {
            Logger.warn("Failed to kick ${target.playerName}: player is not online")
            return
        }

        val msg = getMessage(punishment.type)
        player.kickPlayer(buildMessage(msg, punishment))
        Logger.info("${target.playerName} was ${punishment.type.displayName.lowercase()} by ${punisher.playerName} for ${punishment.reason}")
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

    fun buildRemovalMessage(
        template: String,
        punishment: Punishment,
        user: User,
        senderUser: User,
        reason: String
    ): String {
        val timeLeft = punishment.getDurationFormatted()
        return template
            .replace("%target%", user.playerName)
            .replace("%user%", senderUser.playerName)
            .replace("%reason%", reason)
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

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(Perm.PUNISH_LOG)) {
                val user = UserManager.get(player)
                if (user != null && user.getSetting(PunishmentLogsSetting()) == true) {
                    player.sendMessage(message.translate())
                }
            }
        }

    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}