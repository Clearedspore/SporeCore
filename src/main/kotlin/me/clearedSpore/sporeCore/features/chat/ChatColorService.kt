package me.clearedSpore.sporeCore.features.chat

import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.chat.`object`.ChatColor
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager


object ChatColorService {

    private val config = SporeCore.instance.coreConfig.chat.chatColor

    fun setColor(user: User, color: ChatColor) {
        user.chatColor = color
        UserManager.save(user)
    }

    fun resetColor(user: User){
        user.chatColor = getDefaultColor()
        UserManager.save(user)
    }

    fun getColor(user: User): ChatColor {
        return user.chatColor ?: getDefaultColor()
    }

    fun getDefaultColor(): ChatColor {
        val defaultKey = config.defaultColor
        val defaultConfig = config.colors[defaultKey]
        return ChatColor(
            name = defaultConfig?.name ?: "Gray",
            colorString = defaultConfig?.color ?: "&7",
            material = defaultConfig?.material ?: "GRAY_WOOL"
        )
    }

    fun getAllColors(): List<ChatColor> {
        return config.colors.map { (_, colorConfig) ->
            ChatColor(colorConfig.name, colorConfig.color, colorConfig.material)
        }
    }

    fun getColorByKey(key: String): ChatColor? {
        val colorConfig = config.colors[key.lowercase()] ?: return null
        return ChatColor(colorConfig.name, colorConfig.color, colorConfig.material)
    }
}