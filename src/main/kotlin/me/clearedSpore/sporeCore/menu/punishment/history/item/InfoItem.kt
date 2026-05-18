package me.clearedSpore.sporeCore.menu.punishment.history.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
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
        val item = PlayerUtil.getPlayerHead(target, target.name!!.blue())
        val meta = item.itemMeta

        val user = UserManager.get(target)
        if (user == null) {
            return NoUserItem.toItemStack()
        }

        val total = user.punishments.size
        val bans = user.getPunishmentsByType(PunishmentType.BAN).size
        val warns = user.getPunishmentsByType(PunishmentType.WARN).size
        val mutes = user.getPunishmentsByType(PunishmentType.MUTE).size
        val kicks = user.getPunishmentsByType(PunishmentType.KICK).size
        val lastPunishment = user.getLastPunishment()

        val lore = mutableListOf<String>()
        lore.add("".blue())
        lore.add("Total Punishments: &f$total".blue())
        lore.add("&7| Bans: &f$bans".blue())
        lore.add("&7| Warns: &f$warns".blue())
        lore.add("&7| Mutes: &f$mutes".blue())
        lore.add("&7| Kicks: &f$kicks".blue())
        if (lastPunishment != null) {
            lore.add("".blue())
            lore.add("Last Punishment: ".blue())
            lore.add("&7| Type: &f${lastPunishment.type.displayName}".blue())
            lore.add("&7| Expires: &f${lastPunishment.getDurationFormatted()}".blue())
            val timeAgo = lastPunishment.getTimeSincePunished()
            lore.add("&7| Date: &f${lastPunishment.punishDate} ($timeAgo)".blue())
            lore.add("&7| Reason: &f${lastPunishment.reason}".blue())
            lore.add("&7| Issuer: &f${lastPunishment.getPunisherName(viewer)}".blue())
        }

        meta.lore = lore
        item.itemMeta = meta
        return item

    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}