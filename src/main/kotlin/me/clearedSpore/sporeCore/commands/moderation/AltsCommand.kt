package me.clearedSpore.sporeCore.commands.moderation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeAPI.util.CC.red
import me.clearedSpore.sporeAPI.util.CC.translate
import me.clearedSpore.sporeAPI.util.Task
import me.clearedSpore.sporeCore.user.User
import me.clearedSpore.sporeCore.user.UserManager
import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@CommandAlias("alts")
@CommandPermission(Perm.ALTS)
class AltsCommand : BaseCommand() {

    @Default
    @Syntax("<player>")
    @CommandCompletion("@players")
    fun onAlts(sender: CommandSender, targetName: String) {
        handleAlts(sender, targetName, useDeep = false)
    }

    @Subcommand("deep")
    @Syntax("<player>")
    @CommandPermission(Perm.ALTS_DEEP)
    @CommandCompletion("@players")
    fun onAltsDeep(sender: CommandSender, targetName: String) {
        handleAlts(sender, targetName, useDeep = true)
    }

    private fun handleAlts(sender: CommandSender, targetName: String, useDeep: Boolean) {
        val start = System.currentTimeMillis()
        sender.sendMessage("Checking alts for $targetName...".blue())

        val target = Bukkit.getOfflinePlayer(targetName)
        val targetUser = UserManager.get(target)

        if (targetUser == null) {
            sender.sendMessage("Could not find user $targetName.".red())
            return
        }

        Task.runAsync(Runnable {
            val alts: List<User> = if (useDeep) {
                UserManager.getAltsDeep(targetUser)
            } else {
                UserManager.getAltsByLastIp(targetUser.lastIp.toString(), excludeUuid = targetUser.uuid)
            }

            val end = System.currentTimeMillis()
            val time = end - start
            sender.sendMessage("Took $time ms to check the alts for $targetName".blue())

            val allUsers = listOf(targetUser) + alts

            sender.sendMessage("Linked accounts for ${targetUser.playerName}:".blue())
            allUsers.forEach { user ->
                val onlineStatus = if (user.isOnline()) "&aOnline" else "&7Offline"
                val banned = user.isBanned()
                val muted = user.isMuted()
                val punishmentStatus = when {
                    banned && muted -> "&cBANNED &6MUTED"
                    banned -> "&cBANNED"
                    muted -> "&6MUTED"
                    else -> "&aNo active punishment"
                }

                sender.sendMessage("&f- ".translate() + "${user.playerName} ($onlineStatus, $punishmentStatus".blue() + ")".blue())
            }
        })
    }

}
