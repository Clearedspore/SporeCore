package me.clearedSpore.sporeCore.menu.pms.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Util.wrapWithColors
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class MessageItem(
    val msg: String,
    val index: Int
) : Item() {

    override fun createItem(): ItemStack {
        val wrappedLines = wrapWithColors(msg, 50)

        val item = ItemBuilder(Material.PAPER)
            .setName("Message &f$index".blue())
            .addLoreLine("Message:".gray())

        wrappedLines.forEach {
            item.addLoreLine(it.white())
        }

        item.addLoreLine("")
            .addLoreLine("This message will be deleted".blue())
            .addLoreLine("when you close this menu!!".blue())

        return item.build()
    }


    override fun onClickEvent(clicker: Player, clickType: ClickType) {}
}