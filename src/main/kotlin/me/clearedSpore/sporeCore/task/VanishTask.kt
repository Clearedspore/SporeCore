package me.clearedSpore.sporeCore.task

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.chat.channel.ChatChannelService
import me.clearedSpore.sporeCore.features.vanish.VanishService
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeAPI.util.ActionBar.actionBar
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

object VanishTask {

    private var running = false

    fun start() {
        if (running) return
        running = true


        object : BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    val isVanished = VanishService.isVanished(player.uniqueId)
                    val user = UserManager.get(player)
                    if (user == null) {
                        return
                    }

                    val userChannel = user.channel

                    var channel = "&7Public"

                    if (userChannel != null && ChatChannelService.getChannelByName(userChannel) != null) {
                        channel = ChatChannelService.getChannelByName(userChannel)!!.prefix
                    }

                    if (isVanished) {
                        player.actionBar(
                            "vanish",
                            "Vanished ".blue() + "|".gray() + " Channel: ".blue() + channel.translate(),
                            1500
                        )
                    }
                }
            }
        }.runTaskTimer(SporeCore.instance, 0L, 20L)
    }

    fun stop() {
        if (!running) return
        running = false
    }
}
