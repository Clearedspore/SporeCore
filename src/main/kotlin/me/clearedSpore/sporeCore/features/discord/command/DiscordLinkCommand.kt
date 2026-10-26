package me.clearedSpore.sporeCore.features.discord.command

import me.clearedSpore.sporeCore.features.discord.DiscordService
import me.clearedSpore.sporeCore.features.discord.`object`.DiscordCommand
import me.clearedSpore.sporeCore.features.discord.`object`.DiscordLinks
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
        val code = event.getOption("code")?.asString?.trim()
        if (code == null) {
            event.reply("Please provide a code.").setEphemeral(true).queue()
            return
        }

        val discordId = event.user.id

        if (DiscordLinks.isLinked(discordId)) {
            event.reply("This Discord account is already linked. Use /unlink first.").setEphemeral(true).queue()
            return
        }

        val uuid = DiscordService.consumeCode(code)
        if (uuid == null) {
            event.reply("Invalid or expired code.").setEphemeral(true).queue()
            return
        }

        val user = UserManager.get(uuid)
        if (user == null) {
            event.reply("Couldn't load your Minecraft data. Make sure you're online, then run /link in-game for a new code.")
                .setEphemeral(true).queue()
            return
        }

        user.discordID?.let { DiscordLinks.unlink(it) }   // drop the old link if re-linking
        user.discordID = discordId
        UserManager.save(user)
        DiscordLinks.link(discordId, uuid, user.playerName)

        event.reply("Your Discord account is now linked to **${user.playerName}**.").setEphemeral(true).queue()
    }
}
