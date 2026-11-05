package me.clearedSpore.sporeCore.features.mode.`object`

import de.exlll.configlib.Configuration

@Configuration
data class Mode(
    var name: String = "Default",
    var id: String = "default",
    var weight: Int = 0,
    var commands: List<String> = emptyList(),
    var permission: String = "",
    var gamemode: String = "SURVIVAL",
    var pvp: Boolean = false,
    var vanish: Boolean = false,
    var blockBreak: Boolean = false,
    var blockPlace: Boolean = false,
    var itemPickup: Boolean = false,
    var itemDrop: Boolean = false,
    var silentChest: Boolean = false,
    var inventory: Boolean = false,
    var enableCommands: List<String>? = null,
    var disableCommands: List<String>? = null,
    var blockedCommands: List<String>? = null,
    var chat: Boolean = true,
    var flight: Boolean = false,
    var tpBack: Boolean = true,
    var clearInv: Boolean = false,
    var items: Map<Int, String>? = null
)
