package me.clearedSpore.sporeCore.commands.util

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Name
import co.aikar.commands.annotation.Subcommand
import com.google.gson.Gson
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.ItemUtil
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeAPI.util.Message.sendSuccessMessage
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import me.clearedSpore.sporeCore.util.button.TextButton
import org.bukkit.Material
import org.bukkit.entity.Player

@CommandAlias("util")
@CommandPermission(Perm.UTIL_COMMAND)
@SporeCoreCommand
class UtilItemCommand : BaseCommand() {

    @Subcommand("item toBase64")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onToBase64(player: Player) {
        val item = player.itemInHand

        if (item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!".red())
            return
        }

        val base64 = ItemUtil.itemStackToBase64(item)

        if (base64 == null) {
            player.sendErrorMessage("Failed to load item from Base64!")
            return
        }

        val button = TextButton("[Click to copy]".blue())
            .copyToClipboard(base64)
            .hoverEvent("Click to copy")
            .onClick {
                player.sendSuccessMessage("Successfully copied!")
            }
            .build(player)


        player.sendMessage(button)
    }

    @Subcommand("item fromBase64")
    @CommandPermission(Perm.UTIL_ITEM)
    fun fromBase64(player: Player, @Name("string") base64: String) {
        try {
            val item = ItemUtil.itemStackFromBase64(base64)

            if (item == null) {
                player.sendErrorMessage("Failed to load item from Base64!")
                return
            }

            player.inventory.addItem(item)
            player.sendMessage("Item loaded!")
        } catch (ex: Exception) {
            player.sendErrorMessage("Invalid Base64 data.")
        }
    }


    @Subcommand("item toNBT")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onNBT(player: Player) {
        val item = player.itemInHand
        if (item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!")
            return
        }

        val nbt = ItemUtil.itemToNBTString(item)
        val button = TextButton("[Click to copy]")
            .copyToClipboard(nbt)
            .hoverEvent("Copy NBT")
            .onClick {
                player.sendSuccessMessage("Successfully copied!")
            }
            .build(player)

        player.sendMessage("Item NBT:")
        player.sendMessage(button)
    }

    @Subcommand("item hash")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onHash(player: Player) {
        val item = player.itemInHand

        if (item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!")
            return
        }

        val hash = ItemUtil.hash(item)
        player.sendSuccessMessage("Item hash: $hash".blue())
        val button = TextButton("[Copy]".blue())
            .copyToClipboard(hash)
            .hoverEvent("Click to copy!")
            .onClick {
                player.sendSuccessMessage("Successfully copied!")
            }
            .build(player)
        player.sendMessage(button)
    }

    @Subcommand("item addNBT")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onNBTAdd(player: Player, @Name("key") key: String, @Name("value") value: String) {
        val item = player.itemInHand

        if (item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!")
            return
        }

        try {
            val updatedItem = ItemUtil.addNBTTag(SporeCore.instance, item, key, value)
            player.inventory.setItemInMainHand(updatedItem)
            player.sendSuccessMessage("Successfully added NBT tag!".blue())
        } catch (e: Exception) {
            player.sendErrorMessage("Failed to apply NBT tag!")
        }
    }

    @Subcommand("item hasNBT")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onHasNBT(player: Player, @Name("key") key: String) {
        val item = player.itemInHand

        if (item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!")
            return
        }

        val hasTag = ItemUtil.hasNBTTag(SporeCore.instance, item, key)
        if (hasTag) {
            val value = ItemUtil.getNBTTag(SporeCore.instance, item, key) ?: "null"
            player.sendSuccessMessage("Item has NBT tag '$key' with value '$value'")
        } else {
            player.sendErrorMessage("Item does not have NBT tag '$key'")
        }
    }

    @Subcommand("item fromNBT")
    @CommandPermission(Perm.UTIL_ITEM)
    fun fromNBT(player: Player, @Name("nbt") nbtString: String) {
        try {
            val item = ItemUtil.itemStackFromBase64(nbtString)

            if (item == null || item.type == Material.AIR) {
                player.sendErrorMessage("Failed to load item from NBT!")
                return
            }

            player.inventory.addItem(item)
            player.sendSuccessMessage("Item loaded successfully from NBT!".blue())
        } catch (ex: Exception) {
            player.sendErrorMessage("Invalid NBT data provided!")
        }
    }


    @Subcommand("item clearNBT")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onClearNBT(player: Player) {
        val item = player.itemInHand

        if (item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!")
            return
        }

        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.keys.forEach { meta.persistentDataContainer.remove(it) }
        player.itemInHand.itemMeta = meta
        player.sendSuccessMessage("Cleared all NBT tags from the item.")
    }

    @Subcommand("item listNBT")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onListNBT(player: Player) {
        val item = player.itemInHand
        if (item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!")
            return
        }
        val meta = item.itemMeta ?: return
        val keys = meta.persistentDataContainer.keys
        if (keys.isEmpty()) player.sendErrorMessage("No NBT tags found.")
        else {
            player.sendSuccessMessage("NBT tags: " + keys.joinToString(", ") { it.key })
        }
    }

    @Subcommand("item toJson")
    @CommandPermission(Perm.UTIL_ITEM)
    fun onToJson(player: Player) {
        val item = player.itemInHand

        if (item == null || item.type == Material.AIR) {
            player.sendErrorMessage("You must be holding an item!")
            return
        }

        val serialized = item.serialize().mapValues { (_, v) ->
            if (v is java.util.Optional<*>) v.orElse(null) else v
        }
        val json = Gson().toJson(serialized)
        player.sendSuccessMessage("Item JSON: $json")
        val button = TextButton("[Copy]".blue())
            .copyToClipboard(json)
            .hoverEvent("Click to copy!")
            .onClick {
                player.sendSuccessMessage("Successfully copied!")
            }
            .build(player)
        player.sendMessage(button)
    }


}