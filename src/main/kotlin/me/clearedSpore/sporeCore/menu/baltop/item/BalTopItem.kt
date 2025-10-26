package me.clearedSpore.sporeCore.menu.baltop.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.features.eco.EconomyService
import me.clearedSpore.sporeCore.util.PlayerUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class BalTopItem(
    private val rank: Int,
    private val playerName: String,
    private val balance: Double
) : Item() {

    override fun createItem(): ItemStack {
        val displayName = "#$rank ".white() + playerName.blue()
        val formattedBalance = EconomyService.format(balance)

        val head = PlayerUtil.getPlayerHead(playerName, displayName)
        val meta = head.itemMeta as SkullMeta

        val lore = mutableListOf<String>()
        lore.add("Balance: ".white() + formattedBalance.green())

        meta.lore = lore
        head.itemMeta = meta

        return head
    }


    override fun onClickEvent(clicker: Player, clickType: ClickType) {
    }
}
