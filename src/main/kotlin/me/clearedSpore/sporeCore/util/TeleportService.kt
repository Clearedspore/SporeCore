package me.clearedSpore.sporeCore.util

import me.clearedSpore.sporeAPI.task.SporeScheduler
import me.clearedSpore.sporeAPI.task.Tasks
import me.clearedSpore.sporeAPI.task.Tickable
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.hook.WGUtil
import me.clearedSpore.sporeCore.util.ActionBar.actionBar
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

object TeleportService {

    val teleportTime = SporeCore.instance.coreConfig.general.teleportTime ?: 5
    private val teleportingPlayers = Collections.newSetFromMap(WeakHashMap<Player, Boolean>())
    val worldGuardEnabled = Bukkit.getPluginManager().isPluginEnabled("WorldGuard")

    fun isTeleporting(player: Player) = teleportingPlayers.contains(player)

    fun Player.awaitTeleport(location: Location, seconds: Int = teleportTime) {
        val player = this

        if (isTeleporting(player)) {
            player.sendMessage("You are already teleporting!".red())
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            return
        }

        if (!player.isOnline) return

        if (player.hasPermission(Perm.TELEPORT_BYPASS) || worldGuardEnabled && WGUtil.isInSafeZone(player)) {
            player.teleportAsync(location)
            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
            player.actionBar("tp", "Teleported successfully!".blue())
            return
        }

        teleportingPlayers.add(player)

        SporeScheduler.register(TeleportCountdown(player, location, seconds, teleportingPlayers))
    }
}

class TeleportCountdown(
    private val player: Player,
    private val location: Location,
    private val seconds: Int,
    private val teleportingPlayers: MutableSet<Player>
) : Tickable {

    private val startLocation = player.location.clone()
    private var timeLeft = seconds
    private var tickCounter = 0
    private var finished = false

    override fun isFinished() = finished

    override fun tick() {
        if (!player.isOnline || !teleportingPlayers.contains(player)) {
            cleanup()
            return
        }

        val current = player.location
        val moved = abs(current.x - startLocation.x) > 0.3 || abs(current.z - startLocation.z) > 0.3

        if (moved) {
            Tasks.run {
                teleportingPlayers.remove(player)
                player.actionBar("tp", "Teleportation canceled!".red())
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f)
            }
            finished = true
            return
        }


        tickCounter++
        if (tickCounter < 20) return
        tickCounter = 0

        if (timeLeft <= 0) {
            Tasks.run {
                player.teleportAsync(location)
                teleportingPlayers.remove(player)
                player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                player.actionBar("tp", "Teleported successfully!".blue())
            }
            finished = true
            return
        }

        Tasks.run {
            player.actionBar("tp", "Teleporting in ${timeLeft}s...".blue())
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
        }

        timeLeft--
    }

    private fun cleanup() {
        teleportingPlayers.remove(player)
        finished = true
    }
}