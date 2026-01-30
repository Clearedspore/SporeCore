package me.clearedSpore.sporeCore.util.registry

import co.aikar.commands.BaseCommand
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import java.lang.reflect.Modifier

object CommandRegistry {

    private val rootPath = "me/clearedSpore/"
    val plugin = SporeCore.instance

    fun registerAll(optionalCommands: List<BaseCommand>) {
        Logger.info("Registering commands...")

        var total = 0
        var manual = 0

        RegistryUtil.getAllClassesUnderRoot(rootPath).forEach { clazz ->
            try {
                if (!clazz.isAnnotationPresent(SporeCoreCommand::class.java)) return@forEach
                if (Modifier.isAbstract(clazz.modifiers) || clazz.isInterface) return@forEach

                val instance = clazz.getDeclaredConstructor().newInstance()

                if (instance is BaseCommand) {
                    plugin.commandManager.registerCommand(instance)
                    total++
                } else {
                    Logger.warn("Class ${clazz.name} is annotated with @SporeCoreCommand but does not implement BaseCommand.")
                }
            } catch (ex: Exception) {
                Logger.warn("Failed to register command ${clazz.name}: ${ex.message}")
            }
        }

        optionalCommands.forEach { command ->
            plugin.commandManager.registerCommand(command)
            manual++
        }

        Logger.info("Registered $total commands")
        Logger.info("Registered $manual manual commands")
        Logger.info("A total of ${total + manual} commands were registered")
    }

}