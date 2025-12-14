package me.clearedSpore.sporeCore.menu.invrollback.preview.item

import me.clearedSpore.sporeAPI.menu.Item
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


class InfoItem(
    private val itemName: String,
    private val material: Material,
    private val lore: List<String>
) : Item() {

    override fun createItem(): ItemStack {
        val item = ItemBuilder(material)
            .setName(itemName)
            .setLore(lore)
            .build()
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}