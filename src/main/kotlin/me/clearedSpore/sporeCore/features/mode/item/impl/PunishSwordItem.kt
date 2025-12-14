package me.clearedSpore.sporeCore.features.mode.item.impl
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeCore.features.mode.item.`object`.ModeItem
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack


class PunishSwordItem : ModeItem("punish_sword"), Listener {

    override fun getItemStack(): ItemStack {
        val item = ItemBuilder(Material.DIAMOND_SWORD)
            .addNBTTag("mode_item", id)
            .setName("Punish Sword".blue())
            .addLoreLine("Right click to punish someone".gray())
            .setGlow(true)
            .build()
        return item
    }

    @EventHandler
    fun onRightClick(event: PlayerInteractEntityEvent){
        val player = event.player
        val item = player.itemInHand
        val entity = event.rightClicked

        if(entity !is Player) return
        if(!isItem(item)) return

        if(!canUse(player)) return

        triggerCooldown(player)
        player.performCommand("punish ${entity.name}")
    }
}