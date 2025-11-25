package me.clearedSpore.sporeCore.features.punishment.`object`

import me.clearedSpore.sporeAPI.util.TimeUtil
import me.clearedSpore.sporeCore.database.util.DocWriter
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment.Companion.SYSTEM_UUID
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.entity.Player
import org.dizitart.no2.collection.Document
import java.util.*

data class Punishment(
    var id: String = UUID.randomUUID().toString(),
    var type: PunishmentType,
    var userUuid: UUID,
    var punisherUuid: UUID,
    var expireDate: Date? = null,
    var punishDate: Date = Date(),
    var reason: String,
    var offense: String,
    var removalReason: String? = null,
    var removalUserUuid: UUID? = null,
    var removalDate: Date? = null
) {

    fun getUser(): User? = UserManager.get(userUuid)
    fun getPunisher(): User? = UserManager.get(punisherUuid)

    fun getPunisherName(viewer: Player? = null): String {
        val permission = PunishmentService.config.settings.viewPunisherPermission
        return if (permission == null || viewer?.hasPermission(permission) == true) {
            getPunisher()?.playerName ?: "Hidden"
        } else {
            "Unknown"
        }
    }

    fun getRemovalUser(): User? {
        return removalUserUuid?.let {
            if (it == SYSTEM_UUID) null else UserManager.get(it)
        }
    }

    fun getRemovalUserName(viewer: Player? = null): String {
        val permission = PunishmentService.config.settings.viewPunisherPermission
        return if (permission == null || viewer?.hasPermission(permission) == true) {
            getRemovalUser()?.playerName ?: if (removalUserUuid == SYSTEM_UUID) "System" else "Unknown"
        } else {
            "Unknown"
        }
    }

    fun toDocument(): Document = DocWriter()
        .put("id", id)
        .put("type", type.name)
        .put("userUuid", userUuid.toString())
        .put("punisherUuid", punisherUuid.toString())
        .put("expireDate", expireDate?.time)
        .put("punishDate", punishDate.time)
        .put("reason", reason)
        .put("offense", offense)
        .put("removalReason", removalReason)
        .put("removalUserUuid", removalUserUuid?.toString())
        .put("removalDate", removalDate?.time)
        .build()

    companion object {
        fun fromDocument(doc: Document): Punishment? {
            val id = doc.get("id", String::class.java) ?: return null
            val typeName = doc.get("type", String::class.java) ?: return null
            val type = runCatching { PunishmentType.valueOf(typeName) }.getOrElse { return null }

            val userUuid = runCatching { UUID.fromString(doc.get("userUuid", String::class.java)) }.getOrNull() ?: return null
            val punisherUuid = runCatching { UUID.fromString(doc.get("punisherUuid", String::class.java)) }.getOrNull() ?: return null
            val removalUserUuid = runCatching { doc.get("removalUserUuid", String::class.java)?.let { UUID.fromString(it) } }.getOrNull()

            val expireDate = (doc.get("expireDate") as? Number)?.let { Date(it.toLong()) }
            val punishDate = (doc.get("punishDate") as? Number)?.let { Date(it.toLong()) } ?: Date()
            val reason = doc.get("reason", String::class.java) ?: "No reason provided"
            val offense = doc.get("offense", String::class.java) ?: "unknown"
            val removalReason = doc.get("removalReason", String::class.java)
            val removalDate = (doc.get("removalDate") as? Number)?.let { Date(it.toLong()) }

            return Punishment(
                id = id,
                type = type,
                userUuid = userUuid,
                punisherUuid = punisherUuid,
                expireDate = expireDate,
                punishDate = punishDate,
                reason = reason,
                offense = offense,
                removalReason = removalReason,
                removalUserUuid = removalUserUuid,
                removalDate = removalDate
            )
        }

        val SYSTEM_UUID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }

    fun getDurationFormatted(): String {
        if (removalDate != null) return "Removed"
        val expire = expireDate ?: return "Never"

        val now = System.currentTimeMillis()
        val remaining = expire.time - now

        if (remaining <= 0) return "Expired"

        return TimeUtil.formatDuration(remaining)
    }




    fun getPunishmentDuration(): String {
        return expireDate?.let {
            val durationMillis = it.time - punishDate.time
            TimeUtil.formatDuration(durationMillis)
        } ?: "Permanent"
    }

    fun isExpired(): Boolean {
        return expireDate?.let { Date().after(it) } ?: false
    }

    fun isActive(): Boolean {
        val now = Date()
        if (removalDate != null) return false
        if (expireDate != null && expireDate!!.before(now)) return false
        return true
    }

    fun finalizeExpired() {
        if (expireDate != null && removalDate == null && Date().after(expireDate)) {
            val updated = this.copy(
                removalReason = "Expired",
                removalUserUuid = SYSTEM_UUID,
                removalDate = Date()
            )
            getUser()?.let { user ->
                val index = user.punishments.indexOfFirst { it.id == id }
                if (index != -1) user.punishments[index] = updated
                UserManager.save(user)
            }
        }
    }


    fun getTimeSincePunished(): String {
        val elapsed = System.currentTimeMillis() - punishDate.time
        return TimeUtil.formatDuration(elapsed)
    }

}

