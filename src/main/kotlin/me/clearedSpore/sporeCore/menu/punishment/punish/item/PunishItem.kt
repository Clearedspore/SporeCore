package me.clearedSpore.sporeCore.menu.punishment.punish.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.config.ReasonDefinition
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class PunishItem(
    val player: Player,
    val target: OfflinePlayer,
    val category: String,
    val reasonKey: String,
    val reasonDef: ReasonDefinition
) : Item() {

    override fun createItem(): ItemStack {
        val mat = Material.getMaterial(reasonDef.menu.item.uppercase()) ?: Material.PAPER
        val item = ItemStack(mat)
        val meta = item.itemMeta!!

        meta.setDisplayName(reasonDef.menu.name.translate())
        meta.lore = reasonDef.menu.lore.map { it.translate() }

        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val user = UserManager.get(target)
        val punisher = UserManager.get(clicker)

        if (user == null || punisher == null) {
            clicker.sendMessage("Error fetching user data.".red())
            return
        }


        try {
            PunishmentService.punish(
                targetUser = user,
                punisher = punisher,
                rawReason = reasonKey,
                providedType = null,
                providedTime = null
            )
        } catch (e: Exception) {
            clicker.sendMessage("Failed to punish ${user.playerName}")
        }
        clicker.closeInventory()
    }
}
