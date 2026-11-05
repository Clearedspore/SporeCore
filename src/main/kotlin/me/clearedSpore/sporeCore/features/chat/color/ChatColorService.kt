package me.clearedSpore.sporeCore.features.chat.color

import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.chat.color.`object`.ChatColor
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Util.noTranslate
import org.dizitart.no2.collection.NitriteCollection


object ChatColorService {

    fun setColor(user: User, color: ChatColor) {
        user.chatColor = color
        user.save()
    }

    fun resetColor(user: User) {
        user.chatColor = getDefaultColor()
        user.save()
    }

    fun getColor(user: User): ChatColor {
        return user.chatColor ?: getDefaultColor()
    }

    fun getDefaultColor(): ChatColor {
        val config = SporeCore.instance.coreConfig.chat.chatColor
        val defaultKey = config.defaultColor
        val defaultConfig = config.colors[defaultKey]
        return ChatColor(
            name = defaultConfig?.name ?: "Gray",
            colorString = defaultConfig?.color ?: "&7",
            material = defaultConfig?.material ?: "GRAY_WOOL"
        )
    }

    fun getAllColors(): List<ChatColor> {
        val config = SporeCore.instance.coreConfig.chat.chatColor
        return config.colors.map { (_, colorConfig) ->
            ChatColor(colorConfig.name, colorConfig.color, colorConfig.material)
        }
    }

    fun getColorByKey(key: String): ChatColor? {
        val config = SporeCore.instance.coreConfig.chat.chatColor
        val colorConfig = config.colors[key.lowercase()] ?: return null
        return ChatColor(colorConfig.name, colorConfig.color.noTranslate(), colorConfig.material)
    }
}