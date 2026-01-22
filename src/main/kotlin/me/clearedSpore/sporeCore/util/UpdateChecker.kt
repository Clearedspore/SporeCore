package me.clearedSpore.sporeCore.util

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeCore.SporeCore
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateChecker() : Listener {

    val plugin = SporeCore.instance

    val resourceId: Int = 121185
    var latestVersion: String? = null
    var updateAvailable: Boolean = false

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        checkForUpdates()
    }

    fun checkForUpdates() {
        Task.runAsync(Runnable {
            try {
                val url = URL("https://api.spigotmc.org/legacy/update.php?resource=$resourceId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    latestVersion = reader.readLine()
                }

                val currentVersion = plugin.description.version
                if (currentVersion != latestVersion) {
                    updateAvailable = true

                    Bukkit.getConsoleSender().sendMessage("")
                    Logger.info("§a§lA new update is available for SporeCore!")
                    Logger.info("Current version: $currentVersion")
                    Logger.info("Latest version: $latestVersion")
                    Logger.info("Download the update at: https://modrinth.com/plugin/sporecore/version/latest")
                    Bukkit.getConsoleSender().sendMessage("")
                } else {
                    Logger.info("SporeCore is up to date! (Version: $currentVersion)")
                }

            } catch (e: Exception) {
                Logger.error("Failed to check for updates: ${e.message}")
            }
        })
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player: Player = event.player

        if (updateAvailable && player.hasPermission(Perm.UPDATECHEKER)) {
            Task.runLater(Runnable {
                player.sendMessage("[SporeCore] &fA new update is available!".blue())
                player.sendMessage("[SporeCore] &fCurrent version: &e${plugin.description.version}".blue())
                player.sendMessage("[SporeCore] &fLatest version: &e$latestVersion".blue())
                player.sendMessage("[SporeCore] &fDownload at: &ehttps://modrinth.com/plugin/sporecore/version/latest".blue())
            }, 2)
        }
    }
}