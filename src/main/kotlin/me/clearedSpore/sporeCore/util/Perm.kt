package me.clearedSpore.sporeCore.util

import me.clearedSpore.sporeAPI.util.Logger
import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import kotlin.reflect.full.memberProperties
import kotlin.reflect.KVisibility

object Perm {
    const val MAIN = "sporecore."
    const val BYPASS = MAIN + "bypass."


    const val TELEPORT_BYPASS = BYPASS + "teleport"
    const val PM_BYPASS = BYPASS + "privatemessage"
    const val CHAT_BYPASS = BYPASS + "chat"

    //chat
    const val CHAT = MAIN + "chat."
    const val COLORED_CHAT = CHAT + "colored"


    //gamemode
    const val GAMEMODE  = MAIN + "gamemode"
    const val GAMEMODE_OTHERS  = MAIN + "gamemode.others"
    const val CREATIVE  = MAIN + "gamemode.creative"
    const val SURVIVAL  = MAIN + "gamemode.survival"
    const val ADVENTURE  = MAIN + "gamemode.adventure"
    const val SPECTATOR  = MAIN + "gamemode.spectator"

    //teleport
    const val TELEPORT = MAIN + "teleport"
    const val TELEPORT_CORDS = MAIN + "teleport.coordinates"
    const val TELEPORT_OTHERS = MAIN + "teleport.others"
    const val TELEPORT_ALL = MAIN + "teleport.all"

    //heal & feed
    const val HEAL = MAIN + "heal"
    const val HEAL_OTHERS = MAIN + "heal.others"
    const val FEED = MAIN + "feed"
    const val FEED_OTHERS = MAIN + "feed.others"

    //repair
    const val REPAIR = MAIN + "repair"
    const val REPAIR_OTHERS = MAIN + "repair.others"
    const val REPAIRALL = MAIN + "repairall"
    const val REPAIRALL_OTHERS = MAIN + "repairall.others"

    //utility menus
    const val UTILITY_OTHERS = MAIN + "utility.others"
    const val ANVIL = MAIN + "utility.anvil"
    const val CARTOGRAPHY = MAIN + "utility.cartography"
    const val ENCHANTMENT = MAIN + "utility.enchantmenttable"
    const val GRINDSTONE = MAIN + "utility.grindstone"
    const val LOOM = MAIN + "utility.loom"
    const val SMITHING = MAIN + "utility.smithingtable"
    const val STONECUTTER = MAIN + "utility.stonecutter"
    const val WORKBENCH = MAIN + "utility.workbench"

    //utility permissions
    const val SETSPAWN = MAIN + "spawn.set"
    const val CLEAR_OTHERS = MAIN + "clear.others"
    const val FLIGHT = MAIN + "fly"
    const val FLIGHT_OTHERS = MAIN + "fly.others"
    const val GOD = MAIN + "god"
    const val GOD_OTHERS = MAIN + "god.others"
    const val PWEATHER = "pweather"
    const val PWEATHER_OTHERS = "pweather.others"
    const val PTIME = "ptime"
    const val PTIME_OTHERS = "ptime.others"

    //warps
    const val WARP = MAIN + "warp."
    const val WARP_CREATE = WARP + "create"
    const val WARP_DELETE = WARP + "delete"
    const val WARP_PERMISSION = WARP + "permission"

    //home
    const val HOME = MAIN + "home"

    //kits
    const val KITS = MAIN + "kits"
    const val KIT_ADMIN = "$KITS.admin"

    // /stats for other players
    const val STATS_OTHERS = MAIN + "stats.others"


    //custom currency
    const val CURRENCY = MAIN + "currency."
    const val CURRENCY_GET = CURRENCY + "get"
    const val CURRENCY_GET_OTHERS = CURRENCY + "get.others"
    const val CURRENCY_ADMIN = CURRENCY + "admin"
    const val CURRENCY_TOP = CURRENCY + "top"
    const val CURRENCY_NOTIFY = CURRENCY + "notify"


    //economy
    const val ECO = MAIN + "eco"
    const val ECO_ADMIN = "$ECO.admin"

    //admin permissions
    const val ADMIN = MAIN + "admin"
    const val LOG = MAIN + "log"
    const val ADMIN_LOG = MAIN + "log.admin"


    fun registerAll(default: PermissionDefault = PermissionDefault.OP) {
        val pluginManager = Bukkit.getPluginManager()
        var count = 0

        for (prop in Perm::class.memberProperties) {
            if (prop.visibility == KVisibility.PUBLIC && prop.returnType.classifier == String::class) {
                val value = prop.getter.call() as? String ?: continue
                if (pluginManager.getPermission(value) == null) {
                    pluginManager.addPermission(Permission(value, default))
                    count++
                }
            }
        }

        Logger.info("Registered $count permissions successfully.")
    }

}
