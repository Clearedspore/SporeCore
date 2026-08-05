package me.clearedSpore.sporeCore.menu.punishment.history.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.features.chat.ChatService
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.PlayerUtil
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class InfoItem(
    val viewer: Player,
    val target: OfflinePlayer
) : Item() {

    override fun createItem(): ItemStack {
//        var suffix = ChatService.getSuffix(target as Player)?.translate() ?: ""
        val item = PlayerUtil.getPlayerHead(target, "${target.name}"!!.blue())
        val meta = item.itemMeta

        val user = UserManager.get(target) ?: return NoUserItem.toItemStack()

        val total = user.punishments.size
        val bans = user.getPunishmentsByType(PunishmentType.BAN).size
        val warns = user.getPunishmentsByType(PunishmentType.WARN).size
        val mutes = user.getPunishmentsByType(PunishmentType.MUTE).size
        val kicks = user.getPunishmentsByType(PunishmentType.KICK).size
        val lastPunishment = user.getLastPunishment()

        val lore = mutableListOf<String>()
        lore.add("")
        lore.add("&lTotal Punishments".blue() + ": &f$total".translate())
        lore.add("&7⎜ ".translate() + "&cBans: &f$bans".translate())
        lore.add("&7⎜ ".translate() + "&#FF7A00Warns: &f$warns".translate())
        lore.add("&7⎜ ".translate() + "&eMutes: &f$mutes".translate())
        lore.add("&7⎜ ".translate() + "&aKicks: &f$kicks".translate())
        if (lastPunishment != null) {
            lore.add("")
            lore.add("&lLast Punishment: ".blue())
            lore.add("&7⎜ &fType: ${lastPunishment.type.displayName}".translate())
            lore.add("&7⎜ &fExpires: &e${lastPunishment.getDurationFormatted()}".translate())
            val timeAgo = lastPunishment.getTimeSincePunished()
            lore.add("&7⎜ &fDate: ${lastPunishment.punishDate} &7($timeAgo)".translate())
            lore.add("&7⎜ &fReason: &c${lastPunishment.reason}".translate())
            lore.add("&7⎜ &fIssuer: &e${lastPunishment.getPunisherName(viewer)}".translate())
        }

        meta.lore = lore
        item.itemMeta = meta
        return item

    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}