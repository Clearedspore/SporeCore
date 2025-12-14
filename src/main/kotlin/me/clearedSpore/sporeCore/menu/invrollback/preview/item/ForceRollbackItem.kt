package me.clearedSpore.sporeCore.menu.invrollback.preview.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.Webhook
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.inventory.InventoryManager
import me.clearedSpore.sporeCore.inventory.RestoreMode
import me.clearedSpore.sporeCore.inventory.`object`.InventoryData
import me.clearedSpore.sporeCore.menu.util.confirm.ConfirmMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class ForceRollbackItem(
    private val target: OfflinePlayer,
    private val data: InventoryData
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.RED_WOOL)
            .setName("Forcefully Rollback".red())
            .addLoreLine("Left click to rollback".gray())
            .addLoreLine("".gray())
            .addLoreLine("This will clear the players inventory".gray())
            .addLoreLine("and add the inventory!".gray())
            .build()
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if(!target.isOnline){
            clicker.sendErrorMessage("Player must be online to override!")
            return
        }

        ConfirmMenu(clicker) {
            clicker.sendMessage("Restoring ${target.name}'s inventory...".blue())
            clicker.closeInventory()
            InventoryManager.restoreInventoryFor(target.uniqueId, data, RestoreMode.OVERRIDE)

            val config = SporeCore.instance.coreConfig
            val dcConfig = SporeCore.instance.coreConfig.discord
            if (dcConfig.rollback.isNotEmpty()) {
                try {

                    val webhook = Webhook(dcConfig.rollback)

                    if (dcConfig.rollbackPing.isNotEmpty()) {
                        webhook.setMessage(dcConfig.rollbackPing)
                    }

                    val embed = Webhook.Embed()
                    embed.addField("Issuer", clicker.name)
                    embed.addField("Player", target.name.toString())
                    embed.addField("Claimed", "✔ Yes - Force rollback")
                    embed.setThumbnail(DiscordService.getAvatarURL(target.uniqueId))

                    webhook.setProfileURL(DiscordService.getAvatarURL(clicker.uniqueId))
                    webhook.setUsername(clicker.name)
                    webhook.addEmbed(embed)

                    val messageId = webhook.send()
                    if (messageId != null) {
                        data.messageID = messageId
                        InventoryManager.putCached(data)
                    }

                    data.rollbackIssuer = clicker.name

                    if(config.inventories.deleteAfterRestore) {
                        InventoryManager.removeInventory(data.id)
                    }

                    clicker.sendSuccessMessage("Restored ${target.name}'s inventory!")
                } catch (e: Exception){
                    Logger.error("Failed to send discord message!")
                    e.printStackTrace()
                }
            }
        }.open(clicker)
    }
}