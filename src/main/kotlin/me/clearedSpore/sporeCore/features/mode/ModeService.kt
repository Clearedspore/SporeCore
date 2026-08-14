package me.clearedSpore.sporeCore.features.mode

import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.Extension.uuid
import me.clearedSpore.sporeAPI.exception.LoggedException
import me.clearedSpore.sporeAPI.task.Tasks
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.mode.config.ModeConfig
import me.clearedSpore.sporeCore.features.mode.item.ModeItemManager
import me.clearedSpore.sporeCore.features.mode.`object`.Mode
import me.clearedSpore.sporeCore.features.mode.`object`.ModeData
import me.clearedSpore.sporeCore.features.setting.impl.VanishOnMMLeaveSetting
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Util.parsePlaceholders
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.io.File

object ModeService {

    lateinit var config: ModeConfig
        private set

    val activeModes = mutableMapOf<Player, Mode>()
    private val activeModeData = mutableMapOf<Player, ModeData>()


    fun initialize() {
        loadConfig()
        ModeItemManager.registerItems()
    }


    private fun loadConfig(): ModeConfig {
        val configFile = File(SporeCore.instance.dataFolder, "modes.yml").toPath()
        return try {
            config = YamlConfigurations.update(configFile, ModeConfig::class.java)
            Logger.info("Loaded modes.yml successfully.")
            config
        } catch (ex: ConfigurationException) {
            Logger.error("Invalid modes.yml, using defaults!")
            ex.printStackTrace()
            config = ModeConfig()
            config
        }
    }

    fun enableModeOnJoin(player: Player, mode: Mode) {
        if (activeModes.containsKey(player)) return

        val invData = if (mode.clearInv) {
            InventoryManager.addPlayerInventory(player, "Staff Mode Join")
        } else null

        if (mode.clearInv) {
            InventoryManager.clearPlayerInventory(player)
        }

        activeModes[player] = mode

        applyModeSettings(player, mode.copy(clearInv = false), false)
    }


    fun setMode(player: Player, enabled: Boolean, id: String? = null) {
        if (enabled) {
            val current = activeModes[player]!!
            removeModeSettings(player, current, false)
            activeModes.remove(player)
            activeModeData.remove(player)
        } else {
            val selectedMode = if (id != null) getModeById(id) ?: return else getHighestMode(player)
                ?: return
            activeModes[player] = selectedMode
            applyModeSettings(player, selectedMode, false)
        }
    }

    fun isInStaffInventory(player: Player): Boolean {
        val data = activeModeData[player] ?: return false
        return data.inventoryId != null
    }

    fun forceRestoreAllInventoriesOnShutdown() {
        activeModeData.forEach { (player, data) ->
            if (data.inventoryId != null) {
                InventoryManager.getInventory(data.inventoryId)?.let {
                    InventoryManager.restoreInventory(player, it)
                }
            }
        }
        activeModes.clear()
        activeModeData.clear()
    }


    fun getModes(): Collection<Mode> = config.modes.values

    fun getModeById(id: String): Mode? =
        config.modes[id.lowercase()] ?: config.modes.values.firstOrNull { it.id.equals(id, true) }

    fun getAvailableModes(player: Player): List<Mode> =
        config.modes.values.filter { player.hasPermission(it.permission) }

    fun getHighestMode(player: Player): Mode? =
        getAvailableModes(player).maxByOrNull { it.weight }

    fun getPlayerMode(player: Player): Mode? = activeModes[player]
    fun getPlayerModeData(player: Player): ModeData? = activeModeData[player]
    fun isInMode(player: Player) = activeModes.containsKey(player)


    fun toggleMode(player: Player, id: String? = null, playerIssued: Boolean): Mode? {
        val selectedMode = if (id != null) getModeById(id) ?: return null else getHighestMode(player)
            ?: return null

        val current = activeModes[player]

        if (current != null && current.id.equals(selectedMode.id, true)) {
            removeModeSettings(player, current, playerIssued)
            activeModes.remove(player)
            activeModeData.remove(player)
            return null
        }

        if (current != null && current != selectedMode) {
            removeModeSettings(player, current, playerIssued)
            activeModes.remove(player)
            activeModeData.remove(player)
        }

        activeModes[player] = selectedMode
        applyModeSettings(player, selectedMode, playerIssued)

        return selectedMode
    }


    private fun removeModeSettings(player: Player, mode: Mode, playerIssued: Boolean) {
        val user = UserManager.getIfLoaded(player.uniqueId) ?: return
        try {
            mode.disableCommands?.forEach { cmd ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.name))
            }

            val data = activeModeData[player]

            if (mode.clearInv && data?.inventoryId != null) {
                InventoryManager.getInventory(data.inventoryId)?.let { inv ->
                    InventoryManager.restoreInventory(player, inv)
                }
            }

            if (mode.vanish && !user.getSettingOrDefault(VanishOnMMLeaveSetting()) && playerIssued) {
                VanishService.unVanish(player.uniqueId)
                Bukkit.broadcastMessage(
                    SporeCore.instance.coreConfig.joinLeaveMessages.join
                        .translate()
                        .parsePlaceholders(player))
                if (SporeCore.instance.coreConfig.discord.chat.isNotEmpty()) {
                    val embed = Webhook.Embed()
                        .setColor(0x00FF00)
                        .setDescription("**${player.name} joined the server**")

                    val webhook = Webhook(SporeCore.instance.coreConfig.discord.chat)
                        .setProfileURL(DiscordService.getAvatarURL(player.uniqueId))
                        .setUsername(player.name)
                        .addEmbed(embed)

                    Tasks.runAsync {
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

            data?.let {
                player.gameMode = it.previousGamemode
                player.allowFlight = it.previousFlight
                // Always set this to false!!
                player.isInvulnerable = false
                if (mode.tpBack) {
                    it.previousLocation?.let { loc -> player.teleportAsync(loc) }
                }
            }

            activeModeData.remove(player)

        } catch (ex: Exception) {
            Logger.error("Error removing mode for ${player.name}: ${ex.message}")
            ex.printStackTrace()
        }
    }

    fun disableAll() {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (isInMode(player)) {
                setMode(player, false)
            }
        }
    }

    fun applyModeSettings(player: Player, mode: Mode, playerIssued: Boolean) {
        try {
            val previousGamemode = player.gameMode
            val previousLocation = player.location
            val previousFlight = player.allowFlight
            val previousInvulnerable = player.isInvulnerable
            val isVanished = VanishService.isVanished(player.uniqueId)

            var inventoryId: String? = null

            if (mode.clearInv && !activeModeData.containsKey(player)) {
                val invData = InventoryManager.addPlayerInventory(player, "${mode.id} mode enabled")
                InventoryManager.clearPlayerInventory(player)
                inventoryId = invData.id
            }




            mode.items?.forEach { (slot, itemId) ->

                if (ModeItemManager.getItem(itemId) == null) {
                    Logger.error("Failed to give item ${itemId}: Item does not exist!")
                    return
                }

                ModeItemManager.getItem(itemId)?.let { modeItem ->
                    player.inventory.setItem(slot, modeItem.getItemStack())
                }
            }

            val gamemode = GameMode.valueOf(mode.gamemode.uppercase())
            player.isInvulnerable = true
            player.gameMode = gamemode
            player.allowFlight = mode.flight
            player.isFlying = mode.flight
            player.foodLevel = 20
            player.saturation = 20f

            mode.enableCommands?.forEach { cmd ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.name))
            }

            if (mode.vanish && !isVanished && playerIssued) {
                VanishService.vanish(player.uniqueId)
                Bukkit.broadcastMessage(
                    SporeCore.instance.coreConfig.joinLeaveMessages.leave
                        .translate()
                        .parsePlaceholders(player))

                if (SporeCore.instance.coreConfig.discord.chat.isNotEmpty()) {
                    val embed = Webhook.Embed()
                        .setColor(0xFF0000)
                        .setDescription("**${player.name} left the server**")

                    val webhook = Webhook(SporeCore.instance.coreConfig.discord.chat)
                        .setProfileURL(DiscordService.getAvatarURL(player.uniqueId))
                        .setUsername(player.name)
                        .addEmbed(embed)

                    Tasks.runAsync {
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
            } else if (mode.vanish && !playerIssued) {
                VanishService.vanish(player.uniqueId)
            }

            activeModeData[player] = ModeData(
                mode = mode,
                inventoryId = inventoryId,
                previousGamemode = previousGamemode,
                previousLocation = previousLocation,
                previousFlight = previousFlight,
                previousInvulnerable = previousInvulnerable
            )

        } catch (ex: Exception) {
            Logger.error("Error applying mode for ${player.name}: ${ex.message}")
            ex.printStackTrace()
        }
    }

    fun applyModeSettingsNonDestructive(player: Player, mode: Mode) {
        val previousGamemode = player.gameMode
        val previousLocation = player.location
        val previousFlight = player.allowFlight
        val previousInvulnerable = player.isInvulnerable

        mode.items?.forEach { (slot, itemId) ->
            ModeItemManager.getItem(itemId)?.let {
                player.inventory.setItem(slot, it.getItemStack())
            }
        }

        player.gameMode = GameMode.valueOf(mode.gamemode.uppercase())
        player.allowFlight = mode.flight
        player.isFlying = mode.flight
        player.foodLevel = 20
        player.saturation = 20f

        mode.enableCommands?.forEach { cmd ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.name))
        }

        if (mode.vanish) VanishService.vanish(player.uniqueId)

        activeModeData[player] = ModeData(
            mode = mode,
            inventoryId = null,
            previousGamemode = previousGamemode,
            previousLocation = previousLocation,
            previousFlight = previousFlight,
            previousInvulnerable = previousInvulnerable
        )
    }


    fun enableModeSafely(player: Player, mode: Mode) {
        if (activeModes.containsKey(player)) return
        if (activeModeData.containsKey(player)) return

        activeModes[player] = mode
        applyModeSettingsNonDestructive(player, mode)
    }


}
