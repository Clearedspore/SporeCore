package me.clearedSpore.sporeCore.user

import me.clearedSpore.sporeAPI.exception.LoggedException
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.StringUtil.firstPart
import me.clearedSpore.sporeAPI.util.StringUtil.hasFlag
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.DatabaseManager
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.setting.impl.JoinMsgSetting
import me.clearedSpore.sporeCore.features.setting.impl.StaffmodeOnJoinSetting
import me.clearedSpore.sporeCore.features.setting.impl.TryLogSetting
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Util.parsePlaceholders
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.dizitart.no2.filters.FluentFilter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class UserListener : Listener {

    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player
        val user = UserManager.getOrCreate(player.uniqueId, player.name)

        if (user.playerName != player.name) {
            user.playerName = player.name
        }

        if (user.hasJoinedBefore == false) {
            val firstServerIP = event.hostname
            user.firstServerIP = firstServerIP
        }

        val features = SporeCore.instance.coreConfig.features

        val ip = event.address.hostAddress
        user.lastIp = ip
        if (!user.ipHistory.contains(ip)) user.ipHistory.add(ip)

        val ban = user.getActivePunishment(PunishmentType.BAN)
            ?: user.getActivePunishment(PunishmentType.TEMPBAN)

        if (ban != null && features.punishments) {
            val message = PunishmentService.buildMessage(
                PunishmentService.getMessage(ban.type),
                ban
            )
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message)

            if (PunishmentService.config.settings.notifyTry) {
                val tryTemplate = when (ban.type) {
                    PunishmentType.BAN -> PunishmentService.config.logs.tryBan
                    PunishmentType.TEMPBAN -> PunishmentService.config.logs.tryTempBan
                    else -> null
                }
                tryTemplate?.let {
                    val formatted = PunishmentService.buildTryMessage(it, ban, user)
                    for (player in Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(Perm.PUNISH_LOG)) {
                            val user = UserManager.get(player)
                            if (user != null && user.getSettingOrDefault(TryLogSetting())) {
                                player.sendMessage(formatted.translate())
                            }
                        }
                    }
                }
            }
            return
        }

        val altsOnIp = UserManager.getAltsByLastIp(ip, excludeUuid = user.uuid)
        val bannedAlt = altsOnIp.firstOrNull { it.isBanned() }

        if (bannedAlt != null && features.punishments) {
            val altPunishment = bannedAlt.getActivePunishment(PunishmentType.BAN)
                ?: bannedAlt.getActivePunishment(PunishmentType.TEMPBAN)

            if (altPunishment != null) {
                if (PunishmentService.config.alts.autoBan) {
                    val evasionScreen = PunishmentService.buildAltEvasionScreen(user, altPunishment)
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, evasionScreen.joinToString("\n"))
                }

                if (PunishmentService.config.alts.notifyStaff) {
                    val tryMessage = PunishmentService.buildAltTryMessage(user, altPunishment)
                    Message.broadcastMessageWithPermission(tryMessage, Perm.PUNISH_LOG)
                    Logger.info(tryMessage)
                }

                return
            }
        }

        if (features.invRollback) {
            user.pendingInventories.forEach { id ->
                if (InventoryManager.getInventory(id) == null) {
                    val doc = InventoryManager.inventoryCollection
                        .find(FluentFilter.where("id").eq(id))
                        .firstOrNull()

                    doc?.let {
                        InventoryData.fromDocument(it)?.let { inv ->
                            InventoryManager.putCached(inv)
                        }
                    }
                }
            }
        }

        player.virtualHost?.hostName?.lowercase()?.let {
            user.lastServerIP = it
        }

        user.lastJoin = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        UserManager.save(user)
        UserManager.startAutoSave(user)

        Logger.infoDB("Loaded user data for ${player.name} (${player.uniqueId})")
    }


    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = UserManager.getIfLoaded(player.uniqueId) ?: return
        val config = SporeCore.instance.coreConfig
        val joinConfig = SporeCore.instance.coreConfig.join
        val db = DatabaseManager.getServerData()
        val features = SporeCore.instance.coreConfig.features
        val autoStaff = user.getSettingOrDefault(StaffmodeOnJoinSetting())

        if (autoStaff && player.hasPermission(Perm.MODE_ALLOW)) {
            Logger.log(player, Perm.LOG, "joined the game silently", false)
            event.joinMessage(null)
        } else if (!autoStaff && player.hasPermission(Perm.MODE_ALLOW)) {
            Logger.log(player, Perm.LOG, "joined the server", false) //remove if too repetitive
        }

        if (!user.hasJoinedBefore) {
            user.hasJoinedBefore = true
            user.firstJoin = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            db.totalJoins = db.totalJoins + 1

            if (SporeCore.instance.coreConfig.economy.enabled) {
                val starter = SporeCore.instance.coreConfig.economy.starterBalance
                EconomyService.add(user, starter, "Starter balance")
            }

            joinConfig.firstJoinMessage.forEach { msg ->
                val formatted = msg.replace("%player%", player.name)
                    .replace("%join_count%", db.totalJoins.toString())
                    .translate()
                Bukkit.broadcastMessage(formatted)
            }


            val kitConfig = SporeCore.instance.coreConfig.kits.firstJoinKit
            if (kitConfig.isNotEmpty() && features.kits) {
                val kitName = kitConfig.firstPart()
                val shouldClear = kitConfig.hasFlag("clear")
                val kit = SporeCore.instance.kitService.getAllKits()
                    .find { it.name.equals(kitName, ignoreCase = true) }

                if (kit == null) {
                    Logger.error("Failed to give first join kit to ${player.name}")
                } else {
                    if (shouldClear) player.inventory.clear()
                    SporeCore.instance.kitService.giveKit(player, kitName)
                }
            }
        }

        if (
            features.invRollback &&
            config.inventories.storeReasons.join &&
            !ModeService.isInMode(player)
        ) {
            InventoryManager.addPlayerInventory(player, "Join")
        }



        if (joinConfig.spawnOnJoin && db.spawn != null) {
            player.teleport(db.spawn!!)
        }

        if (joinConfig.title.isNotBlank()) {
            var title = joinConfig.title
            title = title.replace("%player%", player.name)
            player.sendTitle(title.translate(), "")
        }


        if (joinConfig.joinSound.isNotBlank()) {
            runCatching {
                val sound = Sound.valueOf(joinConfig.joinSound)
                player.playSound(player, sound, 1.0f, 1.0f)
            }.onFailure {
                Logger.error("Failed to play join sound: ${joinConfig.joinSound}")
            }
        }


        Task.runLater(Runnable {
            if (joinConfig.message.isNotEmpty() && user.getSettingOrDefault(JoinMsgSetting())) {
                joinConfig.message.forEach { message ->
                    val msg = message
                        .replace("%player%", player.name)
                        .parsePlaceholders(player)
                    player.sendMessage(msg.translate())
                }
            }
        }, 2)

        if (joinConfig.gamemode.isNotBlank()) {
            runCatching {
                val gamemode = GameMode.valueOf(joinConfig.gamemode.uppercase())
                player.gameMode = gamemode
            }.onFailure {
                Logger.error("Failed to apply join gamemode: ${joinConfig.gamemode}")
            }
        }

        Task.runLater({
            if (user.getSettingOrDefault(StaffmodeOnJoinSetting()) &&
                features.modes &&
                player.hasPermission(Perm.MODE_ALLOW)
            ) {
                val mode = ModeService.getHighestMode(player) ?: return@runLater
                ModeService.toggleMode(player, mode.id)
                player.sendMessage("Enabled ${mode.name} mode".blue())
            }
        }, 100, TimeUnit.MILLISECONDS)



        if (
            config.features.vanish &&
            VanishService.vanishedPlayers.isNotEmpty() &&
            !player.hasPermission(Perm.VANISH_SEE)
        ) {
            VanishService.vanishedPlayers
                .mapNotNull { Bukkit.getPlayer(it) }
                .forEach { vanished ->
                    player.hidePlayer(SporeCore.instance, vanished)
                }
        }

        UserManager.save(user)


        val logConfig = config.logs
        if (logConfig.joinLeave) {
            LogsService.addLog(
                player.uuidStr(),
                "Joined the server",
                LogType.JOIN_LEAVE
            )
        }

        if (config.discord.chat.isNotEmpty() && !ModeService.isInMode(player) && !VanishService.isVanished(player.uniqueId)) {
            val embed = Webhook.Embed()
                .setColor(0x00FF00)
                .setDescription("**${player.name} joined the server**")

            val webhook = Webhook(config.discord.chat)
                .setProfileURL(DiscordService.getAvatarURL(player.uniqueId))
                .setUsername(player.name)
                .addEmbed(embed)

            try {
                webhook.send()
            } catch (ex: Exception) {
                throw LoggedException(
                    userMessage = "Failed to send message to Discord.",
                    internalMessage = "Failed to send message to Discord",
                    channel = LoggedException.Channel.GENERAL,
                    developerOnly = false,
                    cause = ex
                ).also { it.log() }
            }
        }
    }


    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = UserManager.getIfLoaded(player.uniqueId) ?: return
        val config = SporeCore.instance.coreConfig
        val features = SporeCore.instance.coreConfig.features

        var wasVanished = false
        Logger.log(player, Perm.LOG, if (features.modes && ModeService.isInMode(player)) "left the game silently" else "left the server" , false)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val joinTime = user.lastJoin?.let {
            runCatching { LocalDateTime.parse(it, formatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
                .getOrNull()
        } ?: System.currentTimeMillis()

        if (features.modes && ModeService.isInMode(player)) {
            event.quitMessage(null)
            wasVanished = true
            ModeService.toggleMode(player)
        }


        if (features.invRollback && config.inventories.storeReasons.leave) {
            InventoryManager.addPlayerInventory(player, "Quit")
        }



        val logConfig = config.logs
        if (logConfig.joinLeave) {
            LogsService.addLog(player.uuidStr(), "Left the server", LogType.JOIN_LEAVE)
        }

        val quitTime = System.currentTimeMillis()
        user.totalPlaytime += quitTime - joinTime
        user.playtimeHistory.add(joinTime to quitTime)

        val twoWeeksAgo = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000)
        user.playtimeHistory.removeIf { it.first < twoWeeksAgo }

        UserManager.save(user)

        UserManager.stopAutoSave(player.uniqueId)
        UserManager.remove(player.uniqueId)

        if (config.discord.chat.isNotEmpty() && !wasVanished) {
            val embed = Webhook.Embed()
                .setColor(0xFF0000)
                .setDescription("**${player.name} left the server**")

            val webhook = Webhook(config.discord.chat)
                .setProfileURL(DiscordService.getAvatarURL(player.uniqueId))
                .setUsername(player.name)
                .addEmbed(embed)

            try {
                webhook.send()
            } catch (ex: Exception) {
                throw LoggedException(
                    userMessage = "Failed to send message to Discord.",
                    internalMessage = "Failed to send message to Discord",
                    channel = LoggedException.Channel.GENERAL,
                    developerOnly = false,
                    cause = ex
                ).also { it.log() }
            }
        }
    }

}
