package me.clearedSpore.sporeCore.acf.targets.resolver

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import me.clearedSpore.sporeCore.acf.TargetType
import me.clearedSpore.sporeCore.acf.targets.impl.TargetPlayersImpl
import me.clearedSpore.sporeCore.acf.targets.`object`.BaseTargetResolver
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import org.bukkit.entity.Player

class PlayerOnlyResolver : BaseTargetResolver<TargetPlayers>(TargetType.PLAYERS_ONLY) {

    override fun getContext(context: BukkitCommandExecutionContext): TargetPlayers {
        val resolved = resolve(context)
            .filterIsInstance<Player>()

        if (resolved.isEmpty()) {
            throw InvalidCommandArgument("No players matched the selector.")
        }

        return TargetPlayersImpl(resolved)
    }
}

