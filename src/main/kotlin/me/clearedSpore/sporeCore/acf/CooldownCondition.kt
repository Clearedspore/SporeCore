package me.clearedSpore.sporeCore.acf

import co.aikar.commands.ConditionFailedException
import co.aikar.commands.PaperCommandManager
import org.bukkit.entity.Player
import java.util.*

object CooldownCondition {
    private val cooldowns = mutableMapOf<UUID, MutableMap<String, Long>>()

    fun register(manager: PaperCommandManager) {
        manager.commandConditions.addCondition(Player::class.java, "cooldown") { context, execContext, player ->

            val targetPlayer = player

            val commandLabel = execContext.args.firstOrNull() ?: "unknown"

            val durationSeconds = context.config?.toDoubleOrNull() ?: 10.0
            val cooldownMillis = (durationSeconds * 1000).toLong()

            val now = System.currentTimeMillis()
            val userCooldowns = cooldowns.computeIfAbsent(targetPlayer.uniqueId) { mutableMapOf() }

            val expiresAt = userCooldowns[commandLabel] ?: 0L
            val remaining = expiresAt - now

            if (remaining > 0) {
                val secondsLeft = "%.1f".format(remaining / 1000.0)
                targetPlayer.sendMessage("§cYou must wait $secondsLeft seconds before using this command again.")
                throw ConditionFailedException("Cooldown active.")
            }

            userCooldowns[commandLabel] = now + cooldownMillis
        }
    }
}
