package me.clearedSpore.sporeCore.menu.rollback.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.features.punishment.`object`.StaffPunishmentStats
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.text.SimpleDateFormat

class StaffRollbackPreviewItem(
    val stat: StaffPunishmentStats,
    val viewer: Player,
    val staff: OfflinePlayer
) : Item() {

    private val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm")

    override fun createItem(): ItemStack {
        val targetUser = UserManager.get(stat.targetUuid)
        val targetName = targetUser?.playerName
            ?: org.bukkit.Bukkit.getOfflinePlayer(stat.targetUuid).name
            ?: stat.targetUuid.toString()

        val active = stat.date.time + 1 > 0
        val item = ItemStack(if (active) Material.LIME_WOOL else Material.GRAY_WOOL)
        val meta = item.itemMeta

        meta.setDisplayName(stat.punishmentId.blue())

        val lore = mutableListOf<String>()
        lore.add("")
        lore.add("Target: &f$targetName".blue())
        lore.add("Type: &f${stat.type.displayName}".blue())
        lore.add("Issuer: &f${staff.name}".blue())
        lore.add("Date: &f${dateFormat.format(stat.date)}".blue())
        lore.add("Reason: &f${stat.reason}".blue())

        meta.lore = lore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}
