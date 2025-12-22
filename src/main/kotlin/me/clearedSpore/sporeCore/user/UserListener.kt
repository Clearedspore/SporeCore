package me.clearedSpore.sporeCore.user

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.StringUtil.firstPart
import me.clearedSpore.sporeAPI.util.StringUtil.hasFlag
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.database.DatabaseManager
import me.clearedSpore.sporeCore.extension.PlayerExtension.uuidStr
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.features.logs.LogsService
import me.clearedSpore.sporeCore.features.logs.`object`.LogType
import me.clearedSpore.sporeCore.features.mode.ModeService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.setting.impl.StaffmodeOnJoinSetting
import me.clearedSpore.sporeCore.features.setting.impl.TryLogSetting
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Tasks
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

class UserListener : Listener {

    @EventHandler
    fun onLogin(event: PlayerLoginEvent) {
        val player = event.player
        val user = UserManager.get(player) ?: return

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

        val logConfig = config.logs
        if (logConfig.joinLeave) {
            LogsService.addLog(
                player.uuidStr(),
                "Joined the server",
                LogType.JOIN_LEAVE
            )
        }

        if (user.pendingPayments.isNotEmpty() && SporeCore.instance.coreConfig.economy.enabled) {
            Tasks.runLater(Runnable {
                player.sendMessage("")
                user.pendingPayments.forEach { (senderName, total) ->
                    val formattedAmount = EconomyService.format(total)
                    player.sendMessage("While you were away you received ${formattedAmount.green()}".blue() + " from ${senderName.white()}".blue())
                }
                player.sendMessage("")
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
                user.pendingPayments.clear()
                UserManager.save(user)
            }, 1)
        }

        if (features.invRollback && config.inventories.storeReasons.join) {
            InventoryManager.addPlayerInventory(player, "Join")
        }

        if (user.getSettingOrDefault(StaffmodeOnJoinSetting()) && features.modes && player.hasPermission(Perm.MODE_ALLOW)) {
            val highest = ModeService.getHighestMode(player)
            if (highest != null) {
                ModeService.toggleMode(player)
                player.sendMessage("Enabled ${highest.name} mode".blue())
            }
        }


        if (config.features.vanish && VanishService.vanishedPlayers.isNotEmpty()) {
            if (player.hasPermission(Perm.VANISH_SEE)) return

            val vanishedPlayers = VanishService.vanishedPlayers
                .mapNotNull { Bukkit.getPlayer(it) }

            vanishedPlayers.forEach { vanished ->
                player.hidePlayer(SporeCore.instance, vanished)
            }
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


        Tasks.runLater(Runnable {
            if (joinConfig.message.isNotEmpty()) {
                joinConfig.message.forEach { message ->
                    val msg = message.replace("%player%", player.name).translate()
                    player.sendMessage(msg)
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

        UserManager.save(user)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        val user = UserManager.getIfLoaded(player.uniqueId) ?: return
        val config = SporeCore.instance.coreConfig
        val features = SporeCore.instance.coreConfig.features

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val joinTime = user.lastJoin?.let {
            runCatching { LocalDateTime.parse(it, formatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
                .getOrNull()
        } ?: System.currentTimeMillis()

        if (features.invRollback && config.inventories.storeReasons.leave) {
            InventoryManager.addPlayerInventory(player, "Quit")
        }

        if (features.modes && ModeService.isInMode(player)) {
            val mode = ModeService.getPlayerMode(player)!!
            if (mode.tpBack) {
                ModeService.toggleMode(player)
            }
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
    }

}
