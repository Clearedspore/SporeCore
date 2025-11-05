package me.clearedSpore.sporeCore.menu.chatcolor

import me.clearedSpore.sporeAPI.menu.BasePaginatedMenu
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.chat.`object`.ChatColor
import me.clearedSpore.sporeCore.menu.chatcolor.item.ColorItem
import me.clearedSpore.sporeCore.menu.chatcolor.item.ResetItem
import me.clearedSpore.sporeCore.menu.chatcolor.item.SpecialColorItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class ChatColorMenu(private val player: Player) : BasePaginatedMenu(SporeCore.instance, true) {

    override fun getMenuName(): String = "Chat Color"
    override fun getRows(): Int = 6

    override fun createItems() {
        val colorConfigs = SporeCore.instance.coreConfig.chat.chatColor.colors

        colorConfigs.forEach { (key, colorConfig) ->
            val color = ChatColor(
                name = colorConfig.name,
                colorString = colorConfig.color,
                material = colorConfig.material
            )
            addItem(ColorItem(key, color, player))
        }

        addSpecialCodeItems()

        setGlobalMenuItem(8, 6, ResetItem(player))
    }

    private fun addSpecialCodeItems() {
        val specialCodes = listOf(
            Triple("Bold", "&l", "ANVIL"),
            Triple("Italic", "&o", "FEATHER"),
            Triple("Underline", "&n", "STICK"),
            Triple("Strikethrough", "&m", "BARRIER"),
            Triple("Magic", "&k", "ENCHANTED_BOOK"),
        )

        var x = 2
        val y = 6
        for ((name, code, mat) in specialCodes) {
            if (x > 8) break
            setGlobalMenuItem(x, y, SpecialColorItem(name, code, mat, player))
            x++
        }
    }

    override fun onInventoryClickEvent(
        clicker: Player,
        clickType: ClickType,
        event: InventoryClickEvent
    ) {}
}
