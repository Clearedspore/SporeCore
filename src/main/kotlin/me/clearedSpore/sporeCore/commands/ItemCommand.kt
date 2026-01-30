package me.clearedSpore.sporeCore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeCore.annotations.SporeCoreCommand
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("i|item|give")
@CommandPermission(Perm.GIVE)
@SporeCoreCommand
class ItemCommand : BaseCommand() {

    @Default
    @CommandCompletion("@materials @players|* @range:1-64 @enchantsWithLevels")
    @Syntax("<material> [player|*] [amount] [enchant(s)|level(s)] [name]")
    fun onGive(sender: Player, vararg args: String) {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: /i <material> [player|*] [amount] [enchant(s)|level(s)] [name]".red())
            return
        }

        var index = 0
        val materialName = args[index++]
        val material = Material.matchMaterial(materialName.uppercase())
        if (material == null) {
            sender.sendMessage("Invalid material: $materialName".red())
            return
        }

        var targets: List<Player> = listOf(sender)
        if (index < args.size) {
            val possibleTarget = args[index]
            if (possibleTarget == "*") {
                targets = sender.server.onlinePlayers.toList()
                index++
            } else {
                val player = sender.server.getPlayer(possibleTarget)
                if (player != null) {
                    targets = listOf(player)
                    index++
                }
            }
        }

        var amount = 64
        if (index < args.size) {
            args[index].toIntOrNull()?.let {
                amount = it.coerceAtMost(64)
                index++
            }
        }

        val enchants: MutableMap<Enchantment, Int> = mutableMapOf()
        if (index < args.size && (args[index].contains("|") || args[index].contains(","))) {
            val enchParts = args[index].split(",")
            for (enchPart in enchParts) {
                val parts = enchPart.split("|")
                val enchName = parts.getOrNull(0)?.uppercase()
                if (enchName != null) {
                    val ench = Enchantment.getByName(enchName)
                    if (ench != null) {
                        val level = parts.getOrNull(1)?.toIntOrNull() ?: 1
                        enchants[ench] = level
                    } else {
                        sender.sendMessage("Invalid enchantment: $enchName".red())
                    }
                }
            }
            index++
        }

        val displayName = if (index < args.size) {
            args.copyOfRange(index, args.size).joinToString(" ").translate()
        } else null

        val item = ItemStack(material, amount)
        val meta = item.itemMeta ?: return
        displayName?.let { meta.setDisplayName(it) }
        enchants.forEach { (ench, level) -> meta.addEnchant(ench, level, true) }
        item.itemMeta = meta

        for (target in targets) {
            target.inventory.addItem(item.clone())
            if (target != sender) {
                sender.sendMessage("Gave ${item.amount}x ${material.name} to ${target.name}!".blue())
            }
            target.sendMessage("You received ${item.amount}x ${material.name}!".blue())
        }
    }

}

