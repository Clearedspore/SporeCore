package me.clearedSpore.sporeCore.menu.investigation.manage.punishment.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.ChatInputService
import me.clearedSpore.sporeAPI.util.Message
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.punishment.PunishmentService
import me.clearedSpore.sporeCore.features.punishment.`object`.Punishment
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType
import me.clearedSpore.sporeCore.menu.investigation.manage.ManageIGMenu
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class LinkedPunishmentItem(
    val investigation: Investigation,
    val punishment: Punishment,
    val viewer: Player,
) : Item() {
    override fun createItem(): ItemStack {
        val active = punishment.isActive()
        val item = ItemBuilder(if (active) Material.LIME_WOOL else Material.GRAY_WOOL)

        item.setName(punishment.id.blue())
        item.addLoreLine("")
        item.addLoreLine("Reason: &f${punishment.reason}".blue())
        item.addLoreLine("Issuer: &f${punishment.getPunisherName(viewer)}".blue())
        val colorCode = if (punishment.isActive()) "&a" else "&c"
        item.addLoreLine("Expires in: $colorCode${punishment.getDurationFormatted()}".blue())
        if (!active) {
            item.addLoreLine("")
            item.addLoreLine("Removal issuer: &f${punishment.getRemovalUserName(viewer)}".blue())
            item.addLoreLine("Removal reason: &f${punishment.removalReason}".blue())
            item.addLoreLine("Removal Date: &f${punishment.removalDate}".blue())
        }

        item.addLoreLine("")
        item.addUsageLine(ClickType.LEFT, "remove the linked punishment")

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
      if(clickType == ClickType.LEFT && clicker.hasPermission(Perm.INVESTIGATION_ADMIN)){
          ConfirmMenu(clicker,{
              IGService.removePunishment(clicker, investigation.id, punishment.id)
              ManageIGMenu(investigation.id, clicker).open(viewer)
              clicker.sendSuccessMessage("Successfully removed linked punishment")
          })
      }
    }
}