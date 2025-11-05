package me.clearedSpore.sporeCore.menu.chatcolor.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.currency.CurrencySystemService
import me.clearedSpore.sporeCore.extension.PlayerExtension.userFail
import me.clearedSpore.sporeCore.features.chat.ChatColorService
import me.clearedSpore.sporeCore.features.chat.ChatService
import me.clearedSpore.sporeCore.features.chat.`object`.ChatColor
import me.clearedSpore.sporeCore.features.chat.`object`.ChatFormat
import me.clearedSpore.sporeCore.menu.util.NoUserItem
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ResetItem(
    private val player: Player
) : Item() {


    override fun createItem(): ItemStack {
        val item = ItemStack(Material.BARRIER)
        val meta = item.itemMeta

        meta.setDisplayName("Reset".blue())
        val lore = mutableListOf<String>()
        lore.add("Reset all color codes".blue())

        meta.lore = lore

        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        val user = UserManager.get(player)

        if(user == null){
            player.closeInventory()
            player.userFail()
            return
        }


        val defaultColor = ChatColorService.getDefaultColor()

        ChatColorService.setColor(user, defaultColor)

        val colorFormat = ChatFormat(
            bold = false,
            italic = false,
            underline = false,
            striketrough = false,
            magic = false,
            none = true
        )

        user.chatFormat = colorFormat

        UserManager.save(user)
        clicker.playSound(clicker.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }
}
