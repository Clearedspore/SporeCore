package me.clearedSpore.sporeCore.menu.punishment.history

import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.punishment.history.item.InfoItem
import me.clearedSpore.sporeCore.menu.punishment.history.item.PunishTypeItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.Date


class HistoryMenu(
    val viewer: Player,
    val target: OfflinePlayer
)
    : Menu(SporeCore.instance) {
    override fun getMenuName(): String {
        return "History | ${target.name}"
    }

    override fun getRows(): Int {
        return 5
    }

    override fun setMenuItems() {

        setMenuItem(5, 2, InfoItem(viewer, target))


        setMenuItem(2, 3, PunishTypeItem(PunishmentType.MUTE, "Mutes", target))
        setMenuItem(4, 3, PunishTypeItem(PunishmentType.BAN, "Bans", target))
        setMenuItem(6, 3, PunishTypeItem(PunishmentType.KICK, "Kicks", target))
        setMenuItem(8, 3, PunishTypeItem(PunishmentType.WARN, "Warns", target))
    }
}