package me.clearedSpore.sporeCore.menu.chatcolor.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.features.chat.`object`.ChatFormat
import me.clearedSpore.sporeCore.user.UserManager
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class SpecialColorItem(
    private val formatKey: String,
    private val codeSymbol: String,
    private val material: String,
    private val player: Player
) : Item() {

    private val permission = "sporecore.chatformat.${formatKey.lowercase()}"

    override fun createItem(): ItemStack {
        val mat = runCatching { Material.valueOf(material.uppercase()) }.getOrElse {
            Logger.warn("Invalid material '$material' for format $formatKey. Defaulting to PAPER.")
            Material.PAPER
        }

        val item = ItemStack(mat)
        val meta = item.itemMeta ?: return item
        val hasPerm = player.hasPermission(permission)
        val user = UserManager.get(player)
        val active = when (formatKey.lowercase()) {
            "bold" -> user?.chatFormat?.bold ?: false
            "italic" -> user?.chatFormat?.italic ?: false
            "underline" -> user?.chatFormat?.underline ?: false
            "strikethrough" -> user?.chatFormat?.striketrough ?: false
            "magic" -> user?.chatFormat?.magic ?: false
            else -> user?.chatFormat?.none ?: true
        }

        meta.setDisplayName("${if (active) "&a" else "&c"}$codeSymbol$formatKey".translate())

        meta.setEnchantmentGlintOverride(active)

        val lore = mutableListOf<String>("")
        lore.add(
            if (hasPerm) {
                if (active) "Click to disable this format.".blue()
                else "Click to enable this format.".blue()
            } else "You don’t have permission for this style.".red()
        )

        meta.lore = lore
        item.itemMeta = meta
        return item
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        if (!clicker.hasPermission(permission)) {
            clicker.sendMessage("You don’t have permission to use this format.".red())
            clicker.playSound(clicker.location, Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        val user = UserManager.get(clicker) ?: return
        val format = user.chatFormat ?: ChatFormat()


        when (formatKey.lowercase()) {
            "bold" -> format.bold = !format.bold
            "italic" -> format.italic = !format.italic
            "underline" -> format.underline = !format.underline
            "strikethrough" -> format.striketrough = !format.striketrough
            "magic" -> format.magic = !format.magic
            "reset" -> {
                format.bold = false
                format.italic = false
                format.underline = false
                format.striketrough = false
                format.magic = false
                format.none = true
            }
        }


        format.none = !listOf(
            format.bold,
            format.italic,
            format.underline,
            format.striketrough,
            format.magic
        ).any { it }

        user.chatFormat = format
        UserManager.save(user)

        val status = if (format.none) "reset all formats" else "toggled ${codeSymbol}${formatKey}"
        clicker.sendMessage("You have $status!".blue())
        clicker.playSound(clicker.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
    }
}
