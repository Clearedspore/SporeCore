package me.clearedSpore.sporeCore.menu.rollback.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.features.punishment.`object`.StaffPunishmentStats
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Tasks
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ConfirmRollbackItem(
    val viewer: Player,
    val staff: OfflinePlayer,
    val timeArg: String,
    val stats: List<StaffPunishmentStats>
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val meta = item.itemMeta
        meta.setDisplayName("§aConfirm Rollback")
        meta.lore = listOf("§7Rollback ${stats.size} punishments")
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        clicker.closeInventory()

        Tasks.runAsync {
            val start = System.currentTimeMillis()
            val senderUser = UserManager.get(clicker) ?: UserManager.getConsoleUser()

            var rollbackCount = 0
            stats.forEach { stat ->
                val targetUser = UserManager.get(stat.targetUuid) ?: return@forEach
                val punishment = targetUser.punishments.firstOrNull { it.id == stat.punishmentId } ?: return@forEach

                val removed = when (punishment.type) {
                    PunishmentType.BAN, PunishmentType.TEMPBAN ->
                        targetUser.unban(UserManager.getConsoleUser(), punishment.id, "Staff rollback - Issued by ${senderUser.playerName}")
                    PunishmentType.MUTE, PunishmentType.TEMPMUTE ->
                        targetUser.unmute(UserManager.getConsoleUser(), punishment.id, "Staff rollback - Issued by ${senderUser.playerName}")
                    PunishmentType.WARN, PunishmentType.TEMPWARN ->
                        targetUser.unwarn(UserManager.getConsoleUser(), punishment.id, "Staff rollback - Issued by ${senderUser.playerName}")
                    else -> false
                }

                if (removed) rollbackCount++
            }

            val end = System.currentTimeMillis()
            clicker.sendMessage(
                "Rolled back $rollbackCount punishments from ${staff.name} in the last $timeArg (took ${end - start}ms).".blue()
            )
            Logger.log(clicker, Perm.ADMIN_LOG, "rolled back punishments made by ${staff.name}", true)
            val config = SporeCore.instance.coreConfig.discord
            val webhook = Webhook(config.staffRollback)
            if(config.staffRollbackPing.isNullOrBlank()) {
                webhook.setMessage("${clicker.name} has rolled back $rollbackCount punishments from ${staff.name} in the last $timeArg")
            } else {
                val ping = config.staffRollbackPing
                webhook.setMessage(
                    "$ping ${clicker.name} has rolled back $rollbackCount punishments from ${staff.name} in the last $timeArg"
                )
            }
            webhook.setUsername("SporeCore Logs")
                .setProfileURL("https://cdn.modrinth.com/data/8X4HqUuD/980c64224cb4fb48829d90a0d51c36b565ad8a05_96.webp")

            webhook.send()
        }
    }
}
