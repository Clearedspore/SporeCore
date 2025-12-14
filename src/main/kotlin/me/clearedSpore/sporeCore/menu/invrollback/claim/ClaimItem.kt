package me.clearedSpore.sporeCore.menu.invrollback.claim

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ClaimItem(
    private val inventoryId: String
) : Item() {

    override fun createItem(): ItemStack =
        ItemBuilder(Material.CHEST)
            .setName("Claim refund".blue())
            .addLoreLine("Click to claim your refund!".gray())
            .addLoreLine("".gray())
            .addLoreLine("&lThis will override your current inventory!!".red())
            .build()

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val user = UserManager.get(clicker)

        if (user == null) {
            clicker.closeInventory()
            clicker.sendErrorMessage("Failed to load user data!")
            return
        }

        if (!user.pendingInventories.contains(inventoryId)) {
            clicker.sendErrorMessage("This inventory has already been claimed.")
            clicker.closeInventory()
            return
        }

        val data = InventoryManager.getInventory(inventoryId)

        if (data == null) {
            user.pendingInventories.remove(inventoryId)
            UserManager.save(user)
            clicker.sendErrorMessage("This inventory no longer exists.")
            clicker.closeInventory()
            return
        }

        ConfirmMenu(clicker) {
            try {
                InventoryManager.restoreInventory(clicker, data)

                user.pendingInventories.remove(inventoryId)
                InventoryManager.removeInventory(inventoryId)
                UserManager.save(user)

                clicker.closeInventory()
                clicker.sendSuccessMessage("Successfully rolled back your inventory!")

                val dcConfig = SporeCore.instance.coreConfig.discord
                if (dcConfig.rollback.isNotEmpty()) {
                    val webhook = Webhook(dcConfig.rollback)
                    val embed = Webhook.Embed()
                    embed.addField("Issuer", data.rollbackIssuer)
                    embed.addField("Player", clicker.name)
                    embed.addField("Claimed", "✔ Yes")
                    embed.setThumbnail(DiscordService.getAvatarURL(clicker.uniqueId))
                    webhook.editMessage(data.messageID, embed)
                }

            } catch (e: Exception) {
                clicker.closeInventory()
                clicker.sendErrorMessage("Failed to claim refund!")
                Logger.error("Failed to restore inventory data")
                e.printStackTrace()
            }
        }.open(clicker)
    }
}
