package me.clearedSpore.sporeCore.features.homes

import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.features.homes.`object`.Home
import me.clearedSpore.sporeCore.user.User
import org.bukkit.Location
import java.util.*
import java.util.concurrent.CompletableFuture

class HomeService {


    fun getAllHomes(user: User): List<Home> {
        return user.homes.toList()
    }


    fun createHome(user: User, name: String, location: Location) {
        val key = name.lowercase()

        if (user.homes.any { it.name.equals(name, ignoreCase = true) }) {
            Logger.warn("Home '$name' already exists for player ${user.playerName}. Skipping creation.")
            return
        }

        val home = Home(name, location)
        user.homes.add(home)
        Logger.info("Created home '$name' for player ${user.playerName} at ${location.world?.name ?: "Unknown world"} [${location.blockX}, ${location.blockY}, ${location.blockZ}]")
        UserManager.save(user)
    }


    fun deleteHome(user: User, name: String) {
        val home = user.homes.find { it.name.equals(name, ignoreCase = true) }
        if (home == null) {
            Logger.warn("Attempted to remove home '$name' for player ${user.playerName}, but it was not found.")
            return
        }

        user.homes.remove(home)
        Logger.info("Deleted home '$name' for player ${user.playerName}.")
        UserManager.save(user)
    }


    fun hasHome(user: User, name: String): Boolean {
        return user.homes.any { it.name.equals(name, ignoreCase = true) }
    }


    fun getHome(user: User, name: String): Home? {
        return user.homes.find { it.name.equals(name, ignoreCase = true) }
    }
}
