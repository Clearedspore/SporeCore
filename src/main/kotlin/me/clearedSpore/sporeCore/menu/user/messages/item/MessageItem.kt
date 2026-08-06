package me.clearedSpore.sporeCore.menu.user.messages.item

import me.clearedSpore.sporeAPI.menu.item.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeAPI.util.time.TimeUtil
import me.clearedSpore.sporeCore.extension.PlayerExtension.toPlayerName
import me.clearedSpore.sporeCore.features.message.Message
import me.clearedSpore.sporeCore.menu.user.messages.MessagesMenu
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeAPI.util.ItemBuilder
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.Util.wrapWithColors
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MessageItem(
    val user: User,
    val message: Message,
    val index: Int,
    val viewer: Player,
    val adminMode: Boolean = false,
) : Item() {

    override fun createItem(): ItemStack {
        val isLong = message.message.length > 50
        val formatted = if(message.raw) message.message.blue() else message.message
        val wrappedLines = if (isLong) emptyList() else wrapWithColors(formatted, 25)

        val age = System.currentTimeMillis() - message.timestamp
        val timeAgo = TimeUtil.formatDuration(age, TimeUtil.TimeUnitStyle.SHORT, 2)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val date = formatter.format(
            Instant.ofEpochMilli(message.timestamp).atZone(ZoneId.systemDefault())
        )

        val item = ItemBuilder(Material.PAPER)
            .setName("Message &f$index".blue())
            .addLoreLine("Timestamp: &f$date ($timeAgo)".blue())
            .addLoreLine("Type: &f${message.type}".blue())

        if (viewer.hasPermission(Perm.ADMIN)) {
            item.addLoreLine("Caller: &f${message.caller.toPlayerName()}".blue())
        }

        item.addLoreLine("Message:".gray())

        if (isLong) {
            item.addLoreLine("The message is too long to be shown in the menu!".red())
        } else {
            wrappedLines.forEach {
                item.addLoreLine(it.white())
            }
        }

        if (adminMode) {
            item.addLoreLine("")
            if(isLong) {
                item.addUsageLine(ClickType.LEFT, "to view in chat")
            }
            item.addUsageLine(ClickType.SHIFT_LEFT, "delete this message")
        }

        return item.build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (clickType == ClickType.SHIFT_LEFT && adminMode) {
            user.messages.remove(message)
            user.save()
            clicker.sendSuccessMessage("Successfully deleted the message!")
            MessagesMenu(user, clicker, true).open(clicker)
            return
        }

        if (clickType == ClickType.LEFT) {
            if (message.message.length > 25) {
                clicker.sendSuccessMessage("Message #$index: ")
                clicker.sendMessage(message.message)
            }
            return
        }
    }
}
