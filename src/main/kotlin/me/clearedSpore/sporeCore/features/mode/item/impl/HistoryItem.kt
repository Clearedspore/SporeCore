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


class HistoryItem : ModeItem("history"), Listener {

    override fun getItemStack(): ItemStack {
        val item = ItemBuilder(Material.BOOK)
            .addNBTTag("mode_item", id)
            .setName("History".blue())
            .addLoreLine("Right click to check someones history".gray())
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
        player.performCommand("history ${entity.name}")
    }
}