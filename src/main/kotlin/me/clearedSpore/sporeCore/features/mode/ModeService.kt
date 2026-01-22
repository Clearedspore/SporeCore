package me.clearedSpore.sporeCore.features.mode

import de.exlll.configlib.ConfigurationException
import de.exlll.configlib.YamlConfigurations
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.mode.config.ModeConfig
import me.clearedSpore.sporeCore.features.mode.item.ModeItemManager
import me.clearedSpore.sporeCore.features.mode.`object`.Mode
import me.clearedSpore.sporeCore.features.mode.`object`.ModeData
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.inventory.InventoryManager
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
            InventoryManager.addPlayerInventory(player, "Staffmode join")
        } else null

        if (mode.clearInv) {
            InventoryManager.clearPlayerInventory(player)
        }

        activeModes[player] = mode

        applyModeSettings(player, mode.copy(clearInv = false))
    }


    fun setMode(player: Player, enabled: Boolean, id: String? = null) {
        if (enabled) {
            val current = activeModes[player]!!
            removeModeSettings(player, current)
            activeModes.remove(player)
            activeModeData.remove(player)
        } else {
            val selectedMode = if (id != null) getModeById(id) ?: return else getHighestMode(player)
                ?: return
            activeModes[player] = selectedMode
            applyModeSettings(player, selectedMode)
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


    fun toggleMode(player: Player, id: String? = null): Mode? {
        val selectedMode = if (id != null) getModeById(id) ?: return null else getHighestMode(player)
            ?: return null

        val current = activeModes[player]

        if (current != null && current.id.equals(selectedMode.id, true)) {
            removeModeSettings(player, current)
            activeModes.remove(player)
            activeModeData.remove(player)
            return null
        }

        if (current != null && current != selectedMode) {
            removeModeSettings(player, current)
            activeModes.remove(player)
            activeModeData.remove(player)
        }

        activeModes[player] = selectedMode
        applyModeSettings(player, selectedMode)

        return selectedMode
    }


    private fun removeModeSettings(player: Player, mode: Mode) {
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

            if (mode.vanish) VanishService.unVanish(player.uniqueId)

            data?.let {
                player.gameMode = it.previousGamemode
                player.allowFlight = it.previousFlight
                player.isInvulnerable = it.previousInvulnerable
                if (mode.tpBack) {
                    it.previousLocation?.let { loc -> player.teleport(loc) }
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

    fun applyModeSettings(player: Player, mode: Mode) {
        try {
            val previousGamemode = player.gameMode
            val previousLocation = player.location
            val previousFlight = player.allowFlight
            val previousInvulnerable = player.isInvulnerable

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
            if (gamemode == null) {
                Logger.error("Failed to apply gamemode!")
            } else {
                player.gameMode = gamemode
            }
            player.isInvulnerable = mode.invulnerable
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
        player.isInvulnerable = mode.invulnerable
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
