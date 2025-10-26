package me.clearedSpore.sporeCore.extension

import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.SporeCore
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


object PlayerExtension {

    fun Player.refresh(){
        Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
            this.hidePlayer(SporeCore.instance, onlinePlayer)
            this.showPlayer(SporeCore.instance, onlinePlayer)
        }
    }

    fun CommandSender.userFail() {
        val prefix = Logger.pluginName
        this.sendMessage("$prefix » ✖ | That player has never joined the server!".red())
        if (this is Player) {
            this.playSound(this.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
        }
    }

}