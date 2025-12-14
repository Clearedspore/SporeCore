package me.clearedSpore.sporeCore.util.button

import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.command.CommandSender
import java.util.UUID
import java.util.concurrent.TimeUnit

object CallbackRegistry {
    private val callbacks = mutableMapOf<String, (CommandSender) -> Unit>()

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build<String, (CommandSender) -> Unit>()

    fun register(sender: CommandSender, fn: (CommandSender) -> Unit): String {
        val id = UUID.randomUUID().toString()
        cache.put(id, fn)
        return id
    }

    fun execute(sender: CommandSender, id: String) {
        cache.getIfPresent(id)?.invoke(sender)
    }
}
