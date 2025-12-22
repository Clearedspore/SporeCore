package me.clearedSpore.sporeCore.acf.targets.resolver

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import me.clearedSpore.sporeCore.acf.TargetType
import me.clearedSpore.sporeCore.acf.targets.impl.TargetSinglePlayerImpl
import me.clearedSpore.sporeCore.acf.targets.`object`.BaseTargetResolver
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetSinglePlayer
import org.bukkit.entity.Player

class SinglePlayerResolver :
    BaseTargetResolver<TargetSinglePlayer>(TargetType.PLAYERS_ONLY) {

    override fun getContext(context: BukkitCommandExecutionContext): TargetSinglePlayer {
        val resolved = resolve(context).filterIsInstance<Player>()

        if (resolved.size != 1) {
            throw InvalidCommandArgument("You must select exactly one player.")
        }

        return TargetSinglePlayerImpl(resolved.first())
    }
}
