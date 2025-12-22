package me.clearedSpore.sporeCore.acf

import co.aikar.commands.*
import me.clearedSpore.sporeAPI.util.CC.red
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ConfirmCondition {
    private val pendingConfirms = ConcurrentHashMap<UUID, Long>()
    private const val CONFIRM_TIMEOUT = 10_000L

    fun register(manager: PaperCommandManager) {
        manager.commandConditions.addCondition(
            Player::class.java,
            "confirm"
        ) { context: ConditionContext<BukkitCommandIssuer>,
            execContext: BukkitCommandExecutionContext,
            value: Player ->

            val player = value
            val lastTime = pendingConfirms[player.uniqueId]
            val currentTime = System.currentTimeMillis()

            if (lastTime == null || currentTime - lastTime > CONFIRM_TIMEOUT) {
                pendingConfirms[player.uniqueId] = currentTime
                player.sendMessage("⚠ Are you sure you want to do this?".red())
                player.sendMessage("⚠ This action cannot be undone!".red())
                player.sendMessage("⚠ Run the command again to confirm.".red())
                throw ConditionFailedException("Confirmation required.")
            } else {
                pendingConfirms.remove(player.uniqueId)
            }
        }
    }
}