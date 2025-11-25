package me.clearedSpore.sporeCore.util

import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import org.apache.commons.codec.language.Soundex
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
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

    fun Player.actionBar(message: String){
        this.sendActionBar(message.blue())
        this.playSound(this, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
    }

    fun Player.actionBarRed(message: String){
        this.sendActionBar(message.red())
        this.playSound(this, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
    }
}
