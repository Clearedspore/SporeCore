package me.clearedSpore.sporeCore.extension

import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*


object PlayerExtension {

    fun Player.refresh() {
        Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
            this.hidePlayer(SporeCore.instance, onlinePlayer)
            this.showPlayer(SporeCore.instance, onlinePlayer)
        }
    }

    fun CommandSender.userJoinFail() {
        val prefix = Logger.pluginName
        this.sendMessage("$prefix » ✖ | That player has never joined the server!".red())
        if (this is Player) {
            this.playSound(this.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
        }
    }

    fun CommandSender.uuid(): UUID {
        return if (this is Player) {
            this.uniqueId
        } else {
            UUID.fromString(UserManager.getConsoleUser().uuidStr)
        }
    }

    fun OfflinePlayer.hasJoinedBefore(): Boolean {
        if (this.isOnline) return true
        if (this.hasPlayedBefore()) return true

        return Bukkit.getPlayer(this.uniqueId) != null
    }

    fun OfflinePlayer.safeUuid(): UUID {
        val user = UserManager.get(this)
        return user?.uuid ?: this.uniqueId
    }

    fun OfflinePlayer.safeUuidStr(): String {
        val user = UserManager.get(this)
        return user?.uuidStr ?: this.uniqueId.toString()
    }

    fun CommandSender.uuidStr(): String {
        return if (this is Player) this.uniqueId.toString() else UserManager.getConsoleUser().uuid.toString()
    }

    fun CommandSender.userFail() {
        val prefix = Logger.pluginName
        this.sendMessage("$prefix » ✖ | Failed to load requested user data!".red())
        if (this is Player) {
            this.playSound(this.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
        }
    }

}