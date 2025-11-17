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
        val type = punishment.type
        val permission: String = when(type) {
            PunishmentType.TEMPMUTE, PunishmentType.MUTE -> Perm.UNMUTE
            PunishmentType.BAN, PunishmentType.TEMPBAN -> Perm.UNBAN
            PunishmentType.WARN, PunishmentType.TEMPWARN -> Perm.UNWARN
            else -> ""
        }


        if(clickType.isRightClick) {
            if (!clicker.hasPermission(permission)) return
            if (!punishment.isActive()) return

            val user = UserManager.get(target)
            if(user == null){
                return
            }

            val clickerUser = UserManager.get(clicker)
            if(clickerUser == null){
                return
            }

            when (type) {
                PunishmentType.TEMPMUTE, PunishmentType.MUTE -> {
                    val activePunishment = user.getActivePunishment(PunishmentType.MUTE)
                    if (activePunishment != null) {
                        SporeCore.instance.chatInput.awaitChatInput(clicker) { input ->
                            val reason = input.takeIf { it.isNotBlank() }
                            if (reason == null) {
                                clicker.sendMessage("You must provide a reason")
                                return@awaitChatInput
                            }

                            val msg = PunishmentService.config.logs.unMute
                            val formatted = PunishmentService.buildRemovalMessage(
                                msg,
                                activePunishment,
                                clickerUser,
                                user
                            )

                            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
                            user.unmute(clickerUser, reason)
                            clicker.sendMessage("Successfully unmuted ${user.playerName}.".blue())
                        }
                    }
                }

                PunishmentType.BAN, PunishmentType.TEMPBAN -> {
                    val activePunishment = user.getActivePunishment(PunishmentType.BAN)
                    if (activePunishment != null) {
                        SporeCore.instance.chatInput.awaitChatInput(clicker) { input ->
                            val reason = input.takeIf { it.isNotBlank() }
                            if (reason == null) {
                                clicker.sendMessage("You must provide a reason")
                                return@awaitChatInput
                            }

                            val msg = PunishmentService.config.logs.unBan
                            val formatted = PunishmentService.buildRemovalMessage(
                                msg,
                                activePunishment,
                                clickerUser,
                                user
                            )

                            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
                            user.unban(clickerUser, reason)
                            clicker.sendMessage("Successfully unbanned ${user.playerName}.".blue())
                        }
                    }
                }
                PunishmentType.WARN, PunishmentType.TEMPWARN -> {
                    val activePunishment = user.getActivePunishment(PunishmentType.WARN)
                    if (activePunishment != null) {
                        SporeCore.instance.chatInput.awaitChatInput(clicker) { input ->
                            val reason = input.takeIf { it.isNotBlank() }
                            if (reason == null) {
                                clicker.sendMessage("You must provide a reason")
                                return@awaitChatInput
                            }

                            val msg = PunishmentService.config.logs.unWarn
                            val formatted = PunishmentService.buildRemovalMessage(
                                msg,
                                activePunishment,
                                clickerUser,
                                user
                            )

                            Message.broadcastMessageWithPermission(formatted, Perm.PUNISH_LOG)
                            user.unwarn(clickerUser, punishment.id, reason)
                            clicker.sendMessage("Successfully unwarned ${user.playerName}.".blue())
                        }
                    }
                }
                else -> {
                    clicker.sendMessage("Failed to remove punishment".red())
                }
            }
        }
    }
}