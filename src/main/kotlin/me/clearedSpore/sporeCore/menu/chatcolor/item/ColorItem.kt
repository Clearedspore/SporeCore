package me.clearedSpore.sporeCore.menu.chatcolor.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.chat.ChatColorService
import me.clearedSpore.sporeCore.features.chat.ChatService
import me.clearedSpore.sporeCore.features.chat.`object`.ChatColor
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ColorItem(
    private val colorKey: String,
    private val color: ChatColor,
    private val player: Player
) : Item() {

    private val permission = "sporecore.chat.color.$colorKey"

    override fun createItem(): ItemStack {
        val material = try {
            Material.valueOf(color.material.uppercase())
        } catch (e: IllegalArgumentException) {
            Logger.warn("Invalid material '${color.material}' in ChatColor config. Defaulting to GRAY_WOOL.")
            Material.GRAY_WOOL
        }

        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item

        val hasPerm = player.hasPermission(permission)
        val user = UserManager.get(player)
        if(user == null){
            return NoUserItem.toItemStack()
        }
        val currentColor = ChatColorService.getColor(user)
        if(currentColor.name == color.name){
            meta.setEnchantmentGlintOverride(true)
        }

        meta.setDisplayName("${color.colorString}${color.name}".translate())

        val lore = mutableListOf<String>()
        lore.add("")

        if (hasPerm) {
            lore.add("Preview:".blue())
            lore.add("${ChatService.getPrefixColor(player)}${player.name}: ${color.colorString}This chat color is so cool!".translate())
            lore.add("")
            lore.add("Click to select this color.".blue())
        } else {
            lore.add("You don't have permission for this color.".red())
        }

        meta.lore = lore
        item.itemMeta = meta

        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (!clicker.hasPermission(permission)) {
            clicker.sendMessage("You don’t have permission to use this color!".red())
            clicker.playSound(clicker.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        val user = UserManager.get(clicker) ?: return
        ChatColorService.setColor(user, color)

        clicker.sendMessage("You have selected the ${color.colorString}${color.name}".blue() + " chat color!".blue())
        clicker.playSound(clicker.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }
}
