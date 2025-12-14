package me.clearedSpore.sporeCore.menu.invrollback.preview.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
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


class SoftRollbackItem(
    private val target: OfflinePlayer,
    private val data: InventoryData
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(Material.LIME_WOOL)
            .setName("Soft Rollback".green())
            .addLoreLine("Left click to rollback".gray())
            .addLoreLine("".gray())
            .addLoreLine("This will make it so the player".gray())
            .addLoreLine("can claim the inventory at any time!".gray())
            .build()
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        ConfirmMenu(clicker) {
            InventoryManager.restoreInventoryFor(target.uniqueId, data, RestoreMode.PENDING)
            clicker.sendMessage("Restoring ${target.name}'s inventory... &7(soft)".blue())
            clicker.closeInventory()

            val config = SporeCore.instance.coreConfig.discord
            if (config.rollback.isNotEmpty()) {
                try {

                    val webhook = Webhook(config.rollback)

                    if (config.rollbackPing.isNotEmpty()) {
                        webhook.setMessage(config.rollbackPing)
                    }

                    val embed = Webhook.Embed()
                    embed.addField("Issuer", clicker.name)
                    embed.addField("Player", target.name.toString())
                    embed.addField("Claimed", "✖ No")
                    embed.setThumbnail(DiscordService.getAvatarURL(target.uniqueId))


                    webhook.setProfileURL(DiscordService.getAvatarURL(clicker.uniqueId))
                    webhook.setUsername(clicker.name)
                    webhook.addEmbed(embed)

                    clicker.closeInventory()

                    val messageId = webhook.send()
                    if (messageId != null) {
                        data.messageID = messageId
                        InventoryManager.putCached(data)
                    }

                    data.rollbackIssuer = clicker.name

                    clicker.sendMessage("Successfully rolled back ${target.name}'s inventory &7(Soft)".blue())
                } catch (e: Exception){
                    Logger.error("Failed to send discord message!")
                    e.printStackTrace()
                }
            }
        }.open(clicker)
    }
}