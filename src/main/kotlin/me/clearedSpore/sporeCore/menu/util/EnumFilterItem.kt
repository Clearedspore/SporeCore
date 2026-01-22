package me.clearedSpore.sporeCore.menu.util

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.gray
import me.clearedSpore.sporeAPI.util.CC.green
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.white
import me.clearedSpore.sporeCore.extension.Extensions.prettyName
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class EnumFilterItem<T : Enum<T>>(
    private val current: T,
    private val values: Array<T>,
    private val title: String,
    private val canUse: (Player, T) -> Boolean = { _, _ -> true },
    private val menuProvider: (Player, T) -> Unit
) : Item() {

    override fun createItem(): ItemStack {
        return ItemBuilder(Material.HOPPER)
            .setName(title.blue())
            .setLore(buildEnumLore(current, values))
            .build()
    }


    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        var index = current.ordinal

        do {
            index = when (clickType) {
                ClickType.RIGHT -> (index - 1 + values.size) % values.size
                else -> (index + 1) % values.size
            }
        } while (!canUse(clicker, values[index]))

        menuProvider(clicker, values[index])
    }

    fun <T : Enum<T>> buildEnumLore(
        current: T,
        values: Array<T>
    ): List<String> {
        val currentLine =
            "▶ ${current.prettyName()} ◀".white()

        val list = values.map { value ->
            if (value == current) {
                "  • ${value.prettyName()}".green()
            } else {
                "  • ${value.prettyName()}".white()
            }
        }

        return listOf(
            currentLine,
            "&7&m----------------".gray()
        ) + list + listOf(
            "",
            "Left click to go forward".gray(),
            "Right click to go backwards".gray()
        )
    }

}
