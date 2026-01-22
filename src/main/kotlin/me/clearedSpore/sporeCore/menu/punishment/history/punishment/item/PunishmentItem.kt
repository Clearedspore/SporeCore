package me.clearedSpore.sporeCore.menu.punishment.history.punishment.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.investigation.list.InvestigationListMenu
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.button.TextButton
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
        val colorCode = if (active) "&a" else "&c"
        val item = ItemBuilder(if (active) Material.LIME_WOOL else Material.GRAY_WOOL)
            .setName(punishment.id.blue())
            .addLoreLine("")
            .addLoreLine("Reason: &f${punishment.reason}".blue())
            .addLoreLine("Issuer: &f${punishment.getPunisherName(viewer)}".blue())
            .addLoreLine("Expires in: $colorCode${punishment.getDurationFormatted()}".blue())
        if (!active) {
            item.addLoreLine("")
                .addLoreLine("Removal issuer: &f${punishment.getRemovalUserName(viewer)}".blue())
                .addLoreLine("Removal reason: &f${punishment.removalReason}".blue())
                .addLoreLine("Removal Date: &f${punishment.removalDate}".blue())
        }

        if (active) {
            item.addUsageLine(ClickType.RIGHT, "remove the punishment")
        }
        if (IGService.isStaff(viewer)
            || viewer.hasPermission(Perm.INVESTIGATION_ADMIN)
            && SporeCore.instance.coreConfig.features.investigation) {
            item.addUsageLine(ClickType.SHIFT_LEFT, "add to an investigation")
        }

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (clickType == ClickType.SHIFT_LEFT &&
            (IGService.isStaff(viewer)
                    || viewer.hasPermission(Perm.INVESTIGATION_ADMIN))
            && SporeCore.instance.coreConfig.features.investigation) {
            InvestigationListMenu(viewer, punishment.id, false).open(viewer)
        } else if (clickType.isRightClick) {

            val type = punishment.type
            val permission = when (type) {
                PunishmentType.MUTE, PunishmentType.TEMPMUTE -> Perm.UNMUTE
                PunishmentType.BAN, PunishmentType.TEMPBAN -> Perm.UNBAN
                PunishmentType.WARN, PunishmentType.TEMPWARN -> Perm.UNWARN
                else -> return
            }

            if (!clicker.hasPermission(permission)) return
            if (!punishment.isActive()) return

            val user = UserManager.get(target) ?: return
            val remover = UserManager.get(clicker) ?: return

            ChatInputService.begin(clicker) { input ->
                val reason = input.takeIf { it.isNotBlank() } ?: run {
                    clicker.sendMessage("You must provide a reason")
                    return@begin
                }

                val success = when (type) {
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
                    return@begin
                }

                val logMsg = when (type) {
                    PunishmentType.MUTE, PunishmentType.TEMPMUTE -> PunishmentService.config.logs.unMute
                    PunishmentType.BAN, PunishmentType.TEMPBAN -> PunishmentService.config.logs.unBan
                    PunishmentType.WARN, PunishmentType.TEMPWARN -> PunishmentService.config.logs.unWarn
                    else -> return@begin
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
}