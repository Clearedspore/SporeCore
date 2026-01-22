package me.clearedSpore.sporeCore.util

import me.clearedSpore.sporeAPI.util.Logger
import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import java.lang.reflect.Modifier

object Perm {
    const val MAIN = "sporecore."
    const val BYPASS = MAIN + "bypass."


    const val TELEPORT_BYPASS = BYPASS + "teleport"
    const val PM_BYPASS = BYPASS + "privatemessage"
    const val CHAT_BYPASS = BYPASS + "chat"
    const val CLEAR_CHAT_BYPASS = BYPASS + "clearchat"
    const val FREEZE_BYPASS = BYPASS + "freeze"

    //chat
    const val CHAT = MAIN + "chat."
    const val COLORED_CHAT = CHAT + "colored"
    const val CHATCOLOR = CHAT + "color"


    //gamemode
    const val GAMEMODE = MAIN + "gamemode"
    const val GAMEMODE_OTHERS = MAIN + "gamemode.others"
    const val CREATIVE = MAIN + "gamemode.creative"
    const val SURVIVAL = MAIN + "gamemode.survival"
    const val ADVENTURE = MAIN + "gamemode.adventure"
    const val SPECTATOR = MAIN + "gamemode.spectator"

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
    const val BACK = MAIN + "back"
    const val SPEED = MAIN + "speed"
    const val SPEED_OTHERS = MAIN + "speed.others"
    const val REBOOT = MAIN + "reboot"
    const val BROADCAST = MAIN + "broadcast"
    const val TRASH = MAIN + "trash"
    const val TRASH_OTHERS = MAIN + "trash.others"
    const val ITEM_EDITOR = MAIN + "itemeditor"
    const val GIVE = MAIN + "give"
    const val UPDATECHEKER = MAIN + "updatechecker"
    const val SUDO = MAIN + "sudo"
    const val PING = MAIN + "ping"
    const val PING_OTHERS = MAIN + "ping.others"
    const val WHOIS = MAIN + "whois"
    const val TPSBAR = MAIN + "tpsbar"

    const val CHANNEL_ALLOW = "sporecore.channel.allow"

    //moderation
    const val PUNISHMENTS = MAIN + "punishments"
    const val PUNISH_LOG = "$PUNISHMENTS.notify"

    const val PUNISH = "$PUNISHMENTS.punish"
    const val MUTE = "$PUNISHMENTS.mute"
    const val TEMP_MUTE = "$PUNISHMENTS.tempmute"
    const val BAN = "$PUNISHMENTS.ban"
    const val TEMP_BAN = "$PUNISHMENTS.tempban"
    const val WARN = "$PUNISHMENTS.warn"
    const val TEMP_WARN = "$PUNISHMENTS.tempwarn"
    const val KICK = "$PUNISHMENTS.kick"
    const val UNBAN = "$PUNISHMENTS.unban"
    const val UNMUTE = "$PUNISHMENTS.unmute"
    const val UNWARN = "$PUNISHMENTS.unwarn"
    const val HISTORY = "$PUNISHMENTS.history"
    const val HISTORY_OTHERS = "$PUNISHMENTS.history.others"
    const val ALTS = "$PUNISHMENTS.alts"
    const val ALTS_DEEP = "$PUNISHMENTS.alts.deep"
    const val FREEZE = MAIN + "freeze"

    const val CLEAR_CHAT = MAIN + "clearchat"

    const val VIEW_LOGS = MAIN + "logs.view"

    //warps
    const val WARP = MAIN + "warp."
    const val WARP_CREATE = WARP + "create"
    const val WARP_DELETE = WARP + "delete"
    const val WARP_PERMISSION = WARP + "permission"

    //discord
    const val DISCORD = MAIN + "discord."
    const val LINK = DISCORD + "link"

    //Vanish & modes
    const val VANISH = MAIN + "vanish"
    const val VANISH_SEE = "$VANISH.see"
    const val VANISH_OTHERS = "$VANISH.others"
    const val MODE = MAIN + "mode."
    const val MODE_ALLOW = MODE + "allow"
    const val MODE_OTHERS = MODE + "others"

    //Inventory
    const val INVENTORY = MAIN + "inventory."
    const val INV_ROLLBACK = INVENTORY + "rollback"
    const val INV_DELETE = INVENTORY + "rollback.delete"
    const val INV_TELEPORT = INVENTORY + "rollback.teleport"
    const val INV_ADMIN = INVENTORY + "admin"

    //util command
    const val UTIL = MAIN + "util."
    const val UTIL_COMMAND = MAIN + "util"
    const val UTIL_ITEM = UTIL + "item"
    const val UTIL_PLAYER = UTIL + "player"
    const val UTIL_WORLD = UTIL + "world"
    const val UTIL_SERVER = UTIL + "server"
    const val UTIL_INVENTORY = UTIL + "inventory"

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
    const val CURRENCY_SPENT = CURRENCY + "topspent"
    const val CURRENCY_BOUGHT = CURRENCY + "topbought"
    const val CURRENCY_NOTIFY = CURRENCY + "notify"


    //economy
    const val ECO = MAIN + "eco"
    const val ECO_ADMIN = "$ECO.admin"

    //admin permissions
    const val ADMIN = MAIN + "admin"
    const val LOG = MAIN + "log"
    const val ADMIN_LOG = MAIN + "log.admin"

    // reports
    const val REPORT = MAIN + "reports."
    const val REPORT_STAFF = REPORT + "staff"
    const val REPORT_ADMIN = REPORT + "admin"
    const val REPORT_SILENT = REPORT + "silent"


    // Investigation
    const val INVESTIGATION = MAIN + "investigation."
    const val INVESTIGATION_STAFF = INVESTIGATION + "staff"
    const val INVESTIGATION_ADMIN = INVESTIGATION + "admin"

    fun registerAll(default: PermissionDefault = PermissionDefault.OP) {
        val pluginManager = Bukkit.getPluginManager()
        var count = 0

        val permClass = Perm::class.java
        for (field in permClass.declaredFields) {
            if (field.type == String::class.java && Modifier.isStatic(field.modifiers)) {
                field.isAccessible = true
                val value = field.get(null) as? String ?: continue
                if (pluginManager.getPermission(value) == null) {
                    pluginManager.addPermission(Permission(value, default))
                    count++
                }
            }
        }

        Logger.info("Registered $count permissions successfully.")
    }
}
