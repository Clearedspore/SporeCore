package me.clearedSpore.sporeCore.util.registry

import me.clearedSpore.sporeCore.SporeCore
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.sequences.forEach

object RegistryUtil {

    val plugin = SporeCore.instance

    fun getAllClassesUnderRoot(rootPath: String): List<Class<*>> {
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