package me.clearedSpore.sporeCore.menu.punishment.history.punishment.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class PunishmentItem(
    val punishment: Punishment,
    val viewer: Player,
    val target: OfflinePlayer
) : Item() {
    override fun createItem(): ItemStack {
        val user = UserManager.get(target)
        if (user == null) {
            return NoUserItem.toItemStack()
        }
        val active = punishment.isActive()
        val item = ItemStack(if (active) Material.LIME_WOOL else Material.GRAY_WOOL)
        val meta = item.itemMeta

        meta.setDisplayName(punishment.id.toString().blue())
        val lore = mutableListOf<String>()
        lore.add("")
        lore.add("Reason: &f${punishment.reason}".blue())
        lore.add("Issuer: &f${punishment.getPunisherName(viewer)}".blue())
        val colorCode = if (punishment.isActive()) "&a" else "&c"
        lore.add("Expires in: $colorCode${punishment.getDurationFormatted()}".blue())
        if (!active) {
            lore.add("")
            lore.add("Removal issuer: &f${punishment.getRemovalUserName(viewer)}".blue())
            lore.add("Removal reason: &f${punishment.removalReason}".blue())
            lore.add("Removal Date: &f${punishment.removalDate}".blue())
        }

        meta.lore = lore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (!clickType.isRightClick) return

        val type = punishment.type
        val permission = when(type) {
            PunishmentType.MUTE, PunishmentType.TEMPMUTE -> Perm.UNMUTE
            PunishmentType.BAN, PunishmentType.TEMPBAN -> Perm.UNBAN
            PunishmentType.WARN, PunishmentType.TEMPWARN -> Perm.UNWARN
            else -> return
        }

        if (!clicker.hasPermission(permission)) return
        if (!punishment.isActive()) return

        val user = UserManager.get(target) ?: return
        val remover = UserManager.get(clicker) ?: return

        SporeCore.instance.chatInput.awaitChatInput(clicker) { input ->
            val reason = input.takeIf { it.isNotBlank() } ?: run {
                clicker.sendMessage("You must provide a reason")
                return@awaitChatInput
            }

            val success = when(type) {
                PunishmentType.MUTE, PunishmentType.TEMPMUTE ->
                    user.unmute(remover, punishment.id, reason)

                PunishmentType.BAN, PunishmentType.TEMPBAN ->
                    user.unban(remover, punishment.id, reason)

                PunishmentType.WARN, PunishmentType.TEMPWARN ->
                    user.unwarn(remover, punishment.id, reason)

                else -> false
            }

            if (!success) {
                clicker.sendMessage("Failed to remove punishment".red())
                return@awaitChatInput
            }

            val logMsg = when(type) {
                PunishmentType.MUTE, PunishmentType.TEMPMUTE -> PunishmentService.config.logs.unMute
                PunishmentType.BAN, PunishmentType.TEMPBAN -> PunishmentService.config.logs.unBan
                PunishmentType.WARN, PunishmentType.TEMPWARN -> PunishmentService.config.logs.unWarn
                else -> return@awaitChatInput
            }

            val formatted = PunishmentService.buildRemovalMessage(
                logMsg,
                punishment,
                user,
                remover,
                reason
            )

            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
            clicker.sendMessage("Successfully removed punishment from ${user.playerName}.".blue())
        }
    }
}