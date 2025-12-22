package me.clearedSpore.sporeCore.acf.error

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.contexts.ContextResolver
import me.clearedSpore.sporeCore.acf.TargetSelectorResolver
import me.clearedSpore.sporeCore.acf.TargetType
import me.clearedSpore.sporeCore.acf.targets.impl.TargetPlayersImpl
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import org.bukkit.entity.Player

class PlayerOnlyResolver : ContextResolver<TargetPlayers, BukkitCommandExecutionContext> {

    override fun getContext(context: BukkitCommandExecutionContext): TargetPlayers {
        val sender = context.sender
        val resolved = TargetSelectorResolver(TargetType.PLAYERS_ONLY).getContext(context)

        if (resolved.isEmpty()) {
            SelectorErrorHandler.sendNoPlayerFound(sender, context.popFirstArg() ?: "Unknown")
            return TargetPlayersImpl(emptyList())
        }

        return TargetPlayersImpl(resolved.filterIsInstance<Player>())
    }
}
