package me.clearedSpore.sporeCore.menu.punishment.history

import io.papermc.paper.command.brigadier.argument.ArgumentTypes.player
import me.clearedSpore.sporeAPI.menu.Menu
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.chat.ChatService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.punishment.history.item.InfoItem
import me.clearedSpore.sporeCore.menu.punishment.history.item.PunishTypeItem
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player


class HistoryMenu(
    val viewer: Player,
    val target: OfflinePlayer
) : Menu(SporeCore.instance) {
//    var suffix = ChatService.getSuffix(target as Player)?.translate() ?: ""
    override fun getMenuName(): String {
        return "History | ${target.name}"
    }

    override fun getRows(): Int {
        return 5
    }

    override fun setMenuItems() {

        setMenuItem(5, 2, InfoItem(viewer, target))


        setMenuItem(2, 3, PunishTypeItem(PunishmentType.MUTE, "&e&lMutes".translate(), target))
        setMenuItem(4, 3, PunishTypeItem(PunishmentType.BAN, "&c&lBans".translate(), target))
        setMenuItem(6, 3, PunishTypeItem(PunishmentType.KICK, "&a&lKicks".translate(), target))
        setMenuItem(8, 3, PunishTypeItem(PunishmentType.WARN, "&#FF7A00&lWarns".translate(), target))
    }
}