package me.clearedSpore.sporeCore.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object PlayerUtil {


    fun getPlayerHead(player: OfflinePlayer, displayName: String? = null): ItemStack {
        val head = ItemStack(Material.PLAYER_HEAD)
        val meta = head.itemMeta as SkullMeta

        val profile = Bukkit.createProfile(player.uniqueId, player.name)

        profile.complete(true)

        meta.playerProfile = profile

        if (displayName != null) {
            meta.setDisplayName(displayName)
        }

        head.itemMeta = meta
        return head
    }


    fun getPlayerHead(playerName: String, displayName: String? = null): ItemStack {
        val offline = Bukkit.getOfflinePlayer(playerName)
        return getPlayerHead(offline, displayName)
    }
}
