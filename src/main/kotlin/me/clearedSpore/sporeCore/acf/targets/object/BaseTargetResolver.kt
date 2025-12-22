package me.clearedSpore.sporeCore.acf.targets.`object`

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.contexts.ContextResolver
import me.clearedSpore.sporeCore.acf.TargetSelectorResolver
import me.clearedSpore.sporeCore.acf.TargetType
import org.bukkit.entity.Entity

abstract class BaseTargetResolver<T>(
    private val targetType: TargetType
) : ContextResolver<T, BukkitCommandExecutionContext> {

    protected fun resolve(context: BukkitCommandExecutionContext): Collection<Entity> {
        return TargetSelectorResolver(targetType).getContext(context)
    }
}