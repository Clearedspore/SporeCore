package me.clearedSpore.sporeCore.acf.extension

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import me.clearedSpore.sporeCore.features.investigation.`object`.enum.InvestigationPriority

object ACFExtension {

    inline fun <reified T : Enum<T>> PaperCommandManager.registerEnum() {
        commandContexts.registerContext(T::class.java) { context ->
            val input = context.popFirstArg()
            try {
                enumValueOf<T>(input)
            } catch (e: IllegalArgumentException) {
                throw InvalidCommandArgument("Value not found: $input", false)
            }
        }
    }

    fun <T : Any> PaperCommandManager.registerRegistryType(
        clazz: Class<T>,
        resolver: (String) -> T?
    ) {
        commandContexts.registerContext(clazz) { context ->
            val input = context.popFirstArg()
            resolver(input) ?: throw InvalidCommandArgument("Value not found: $input", false)
        }
    }

}