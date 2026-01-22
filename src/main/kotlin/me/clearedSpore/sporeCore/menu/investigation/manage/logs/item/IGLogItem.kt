package me.clearedSpore.sporeCore.menu.investigation.manage.logs.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.features.investigation.`object`.log.IGLog
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class IGLogItem(
    val investigation: Investigation,
    val log: IGLog,
    val viewer: Player
) : Item() {

    override fun createItem(): ItemStack {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val date = formatter.format(
            Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault())
        )

        return ItemBuilder(Material.PAPER)
            .setName("$date".blue())
            .addLoreLine("")
            .addLoreLine("Log Type: &f${log.type.displayName}".blue())
            .addLoreLine("User: &f${investigation.getName(log.user)}".blue())
            .addLoreLine("Description: &f${log.action}".blue())
            .addLoreLine("ID: &f${log.id}".blue())
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
    }
}