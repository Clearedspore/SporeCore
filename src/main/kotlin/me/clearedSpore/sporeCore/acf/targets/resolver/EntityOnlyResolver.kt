package me.clearedSpore.sporeCore.acf.targets.resolver

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.InvalidCommandArgument
import me.clearedSpore.sporeCore.acf.TargetType
import me.clearedSpore.sporeCore.acf.targets.impl.TargetEntitiesImpl
import me.clearedSpore.sporeCore.acf.targets.`object`.BaseTargetResolver
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetEntities
import org.bukkit.entity.Player

class EntityOnlyResolver :
    BaseTargetResolver<TargetEntities>(TargetType.ENTITIES_ONLY) {

    override fun getContext(context: BukkitCommandExecutionContext): TargetEntities {
        val resolved = resolve(context).filter { it !is Player }

        if (resolved.isEmpty()) {
            throw InvalidCommandArgument("No entities matched the selector.")
        }

        return TargetEntitiesImpl(resolved)
    }
}
