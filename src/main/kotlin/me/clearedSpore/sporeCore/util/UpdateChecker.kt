package me.clearedSpore.sporeCore.util

import com.google.gson.JsonParser
import me.clearedSpore.sporeAPI.annotation.RegisterListener
import me.clearedSpore.sporeAPI.task.Tasks
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Logger

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

@RegisterListener
class UpdateChecker : Listener {

    val plugin get() = SporeCore.instance

    val projectSlug: String = "sporecore"
    var latestVersion: String? = null
    var updateAvailable: Boolean = false

    init {
        checkForUpdates()
    }

    fun checkForUpdates() {
        Tasks.runAsync {
            try {
                val url = URL("https://api.modrinth.com/v2/project/$projectSlug/version?include_changelog=false")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "SporeCore/${plugin.description.version} (UpdateChecker)")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val versions = JsonParser.parseString(response).asJsonArray

                if (versions.size() > 0) {
                    latestVersion = versions[0].asJsonObject.get("version_number").asString
                }

                val currentVersion = plugin.description.version
                if (latestVersion != null && currentVersion != latestVersion) {
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
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player: Player = event.player

        if (updateAvailable && player.hasPermission(Perm.UPDATECHEKER)) {
            Tasks.runLater(2) {
                player.sendMessage("[SporeCore] &fA new update is available!".blue())
                player.sendMessage("[SporeCore] &fCurrent version: &e${plugin.description.version}".blue())
                player.sendMessage("[SporeCore] &fLatest version: &e$latestVersion".blue())
                player.sendMessage("[SporeCore] &fDownload at: &ehttps://modrinth.com/plugin/sporecore/version/latest".blue())
            }
        }
    }
}