package me.clearedSpore.sporeCore.acf.targets.resolver

import co.aikar.commands.BukkitCommandExecutionContext
import me.clearedSpore.sporeCore.acf.TargetType
import me.clearedSpore.sporeCore.acf.targets.impl.TargetEntitiesImpl
import me.clearedSpore.sporeCore.acf.targets.`object`.BaseTargetResolver
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetEntities

class AnyTargetResolver :
    BaseTargetResolver<TargetEntities>(TargetType.ALL) {

    override fun getContext(context: BukkitCommandExecutionContext): TargetEntities {
        return TargetEntitiesImpl(resolve(context).toList())
    }
}
