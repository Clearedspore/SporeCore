package me.clearedSpore.sporeCore.features.discord.`object`

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.user.UserManager
import org.dizitart.no2.collection.NitriteCollection
import org.dizitart.no2.filters.FluentFilter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object DiscordLinks {
    data class Link(val uuid: UUID, var name: String)

    private val byDiscordId = ConcurrentHashMap<String, Link>()
    private val discordIdByUuid = ConcurrentHashMap<UUID, String>()

    fun load(collection: NitriteCollection) {
        var count = 0
        collection.find().forEach { doc ->
            val discordId = doc.get("discordID")?.toString()
            val uuid = doc.get("uuidStr")?.toString()
            val name = doc.get("playerName")?.toString()
            if (!discordId.isNullOrEmpty() && uuid != null) {
                link(discordId, UUID.fromString(uuid), name ?: "Unknown")
                count++
            }
        }
        Logger.infoDB("Loaded $count Discord links")
    }

    fun link(discordId: String, uuid: UUID, name: String) {
        byDiscordId[discordId] = Link(uuid, name)
        discordIdByUuid[uuid] = discordId
    }

    fun unlink(discordId: String) {
        byDiscordId.remove(discordId)?.let { discordIdByUuid.remove(it.uuid) }
    }

    fun get(discordId: String): Link? {
        byDiscordId[discordId]?.let { return it }
        val doc = UserManager.userCollection
            .find(FluentFilter.where("discordID").eq(discordId))
            .firstOrNull() ?: return null
        val uuid = doc.get("uuidStr")?.toString() ?: return null
        link(discordId, UUID.fromString(uuid), doc.get("playerName")?.toString() ?: "Unknown")
        return byDiscordId[discordId]
    }

    fun isLinked(discordId: String) = get(discordId) != null

    fun discordIdFor(uuid: UUID): String? = discordIdByUuid[uuid]

    // Minecraft names can change, so refresh the cached name on join
    fun updateName(uuid: UUID, name: String) {
        discordIdByUuid[uuid]?.let { byDiscordId[it]?.name = name }
    }
}