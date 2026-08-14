package me.clearedSpore.sporeCore.features.discord

import me.clearedSpore.sporeAPI.task.Tasks
import me.clearedSpore.sporeAPI.util.Logger
import me.clearedSpore.sporeCore.SporeCore
import me.clearedSpore.sporeCore.features.discord.command.DiscordLinkCommand
import me.clearedSpore.sporeCore.features.discord.`object`.DiscordCommand
import me.clearedSpore.sporeCore.user.UserManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object DiscordService : ListenerAdapter() {

    private val commands = ConcurrentHashMap<String, DiscordCommand>()
    var initialized = false
    private var token: String = ""

    private val codes = ConcurrentHashMap<String, UUID>()
    private val expiry = ConcurrentHashMap<String, Long>()

    fun start() {
        val config = SporeCore.instance.coreConfig.discord
        if (initialized) return
        if (!config.enabled) return

        token = config.botToken

        if (!validateConfig(config.botToken)) {
            Logger.error("[Discord] Invalid bot token. Discord features disabled.")
            return
        }

        initialized = true

        registerCommands()

        val jda = JDABuilder.createDefault(token)
            .addEventListeners(this)
            .build()

        jda.updateCommands().addCommands(
            commands.values.map { cmd ->
                val slash = Commands.slash(cmd.name, cmd.description)
                cmd.options.forEach { opt ->
                    slash.addOption(
                        OptionType.STRING,
                        opt.name,
                        opt.description,
                        opt.required
                    )
                }
                slash
            }
        ).queue()
        JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT)

        Logger.info("[Discord] Bot started successfully.")
    }

    fun validateConfig(token: String): Boolean {
        if (token.isBlank()) return false
        return try {
            val jda = JDABuilder.createLight(token).build()
            jda.awaitReady()

            jda.shutdownNow()
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun getAvatarURL(UUID: UUID): String {
        return "https://mc-heads.net/avatar/${UUID}/100"
    }

    fun getConsoleAvatar() : String {
        return "https://mc-heads.net/avatar/Console/100"
    }

    fun registerCommands() {
        register(DiscordLinkCommand())
    }

    fun register(command: DiscordCommand) {
        commands[command.name.lowercase()] = command
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val cmd = commands[event.name.lowercase()] ?: return
        cmd.execute(event)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val config = SporeCore.instance.coreConfig.discord
        if (config.chatID.isEmpty()) return
        if (event.channel.id != config.chatID) return
        if (event.author.isBot) return

        Bukkit.getLogger().warning("[Discord] DEBUG: \nAuthor: ${event.author.name}\nMessage: ${event.message.contentRaw}\nChannel: ${event.channel.id}")
    }

    fun hasCode(player: UUID): Boolean {
        return codes.containsValue(player)
    }

    fun generateCode(player: UUID): String {
        val code = (100000..999999).random().toString()
        codes[code] = player
        expiry[code] = System.currentTimeMillis() + 5 * 60_000
        return code
    }

    fun validateCode(code: String): UUID? {
        val exp = expiry[code] ?: return null
        if (System.currentTimeMillis() > exp) {
            codes.remove(code)
            expiry.remove(code)
            return null
        }
        return codes[code]
    }

    fun consumeCode(code: String): UUID? {
        val uuid = validateCode(code) ?: return null
        codes.remove(code)
        expiry.remove(code)
        return uuid
    }
}
