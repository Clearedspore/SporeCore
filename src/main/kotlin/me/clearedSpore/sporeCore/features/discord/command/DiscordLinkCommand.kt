package me.clearedSpore.sporeCore.features.discord.command

import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.discord.`object`.DiscordCommand
import me.clearedSpore.sporeCore.features.discord.`object`.DiscordOption
import me.clearedSpore.sporeCore.user.UserManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class DiscordLinkCommand : DiscordCommand(
    name = "link",
    description = "Link your Minecraft account"
) {
    override val options = listOf(
        DiscordOption("code", "Your linking code", true)
    )

    override fun execute(event: SlashCommandInteractionEvent) {
        val code = event.getOption("code")?.asString ?: return

        val uuid = DiscordService.consumeCode(code)
        if (uuid == null) {
            event.reply("Invalid or expired code.").setEphemeral(true).queue()
            return
        }

        val user = UserManager.get(uuid)
        if (user == null) {
            event.reply("Could not load your Minecraft user data.").setEphemeral(true).queue()
            return
        }

        user.discordID = event.user.id
        UserManager.save(user)

        event.reply("Your Discord account is now linked to **${user.playerName}**.").setEphemeral(true).queue()
    }
}
