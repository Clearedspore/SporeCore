package me.clearedSpore.sporeCore.util.registry

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.AutoListener
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.JarFile

object ListenerRegistry {

    val plugin = SporeCore.instance
    private val rootPath = "me/clearedSpore/"

    fun registerAll() {
        Logger.info("Registering listeners...")

        var total = 0

        getAllClassesUnderRoot().forEach { clazz ->
            try {
                if (!clazz.isAnnotationPresent(AutoListener::class.java)) return@forEach
                if (Modifier.isAbstract(clazz.modifiers) || clazz.isInterface) return@forEach

                val instance = clazz.getDeclaredConstructor().newInstance()

                if (instance is Listener) {
                    Bukkit.getPluginManager().registerEvents(instance, plugin)
                    total++
                } else {
                    Logger.warn("Class ${clazz.name} is annotated with @AutoListener but does not implement Listener")
                }
            } catch (ex: Exception) {
                Logger.warn("Failed to register listener ${clazz.name}: ${ex.message}")
            }
        }

        Logger.info("Registered $total listeners")
    }

    private fun getAllClassesUnderRoot(): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        val classLoader = plugin.javaClass.classLoader

        if (classLoader is URLClassLoader) {
            classLoader.urLs.forEach { url ->
                val file = File(url.toURI())
                if (!file.isFile || file.extension != "jar") return@forEach

                JarFile(file).use { jar ->
                    jar.entries().asSequence()
                        .filter { it.name.endsWith(".class") }
                        .filter { it.name.startsWith(rootPath) }
                        .filter { !it.name.contains("module-info") }
                        .forEach { entry ->
                            val className = entry.name
                                .removeSuffix(".class")
                                .replace('/', '.')

                            try {
                                val clazz = Class.forName(className, false, classLoader)
                                classes.add(clazz)
                            } catch (_: Throwable) {
                            }
                        }
                }
            }
        }

        return classes
    }
}