package me.clearedSpore.sporeCore.features.punishment.config

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration
import me.clearedSpore.sporeCore.database.Database
import me.clearedSpore.sporeCore.features.punishment.`object`.PunishmentType

@Configuration
data class PunishmentConfig(

    var settings: SettingsConfig = SettingsConfig(),

    var alts: AltsConfig = AltsConfig(),

    var discord: DiscordConfig = DiscordConfig(),

    @Comment(
        "You can set the screens or messages that",
        "players get when they are punished.",
        "In the messages you can use the following placeholders:",
        "%reason% -> Reason why someone got punished",
        "%punisher% -> Staff member who punished",
        "%timeleft% -> How much time the user has left for their punishment",
        "              If the punishment is permanent it will say 'never'",
        "%id% -> Punishment ID",
        "%date% -> Time when the player got punished",
    )
    var messages: MessagesConfig = MessagesConfig(),

    var logs: PunishmentLogConfig = PunishmentLogConfig(),

    var reasons: ReasonsConfig = ReasonsConfig(),


    @Comment(
        "These are optional reasons you can",
        "user when removing a punishment.",
        "These reasons are only here for",
        "command suggestions!!!"
    )
    var removalReasons: RemovalReasonConfig = RemovalReasonConfig()
)

@Configuration
data class DiscordConfig(

    @Comment(
        "If the player needs to have their discord account",
        "linked before being able to punish someone."
    )
    var requireLinked: Boolean = false
)

@Configuration
data class AltsConfig(

    @Comment(
        "If a player is banned and tries to join with an alt",
        "should the system automatically block the alt from joining?"
    )
    var autoBan: Boolean = true,

    @Comment(
        "If a player that is banned tries to join with an alt",
        "should the system notify staff?"
    )
    var notifyStaff: Boolean = true,

    @Comment(
        "Notify message for staff.",
        "Placeholders:",
        "%user%, %alt%, %reason%, %time%"
    )
    var tryMessage: String = "&9[SporeCore] &f%user% &7tried to join while having a banned alt &c(%alt%)&7. Reason: &e%reason%&7 | Expires: &e%time%"

)

@Configuration
data class SettingsConfig(
    @Comment("Players must provide a reason if the reason does not exist in the config.")
    var requireReason: Boolean = true,

    @Comment("Whether players can punish themselves.")
    var selfPunish: Boolean = false,

    @Comment(
        "Players with this permission cannot be punished.",
        "You can clear this if you don't want a permission bypass"
    )
    var permBypass: String = "sporecore.punishments.bypass",

    @Comment(
        "If staff should be notified when joining while",
        "being banned or try to talk while being muted."
    )
    var notifyTry: Boolean = true,


    @Comment(
        "Permission required to see who punished or removed a punishment.",
        "Players without this permission will see 'Hidden' instead of staff names."
    )
    var viewPunisherPermission: String = "sporecore.punishments.viewpunisher",

    @Comment(
        "How many lines will it spam to clear the chat?"
    )
    var clearLines: Int = 100
)

@Configuration
data class RemovalReasonConfig(
    var reasons: List<String> = listOf(
        "Appealed",
        "False",
        "Mistake",
        "Shortening"
    )
)

@Configuration
data class MessagesConfig(

    var kick: List<String> = listOf(
        "&cYou have been kicked!",
        "",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "",
        "&fYou can appeal at: &9discord.gg/"
    ),

    var tempBan: List<String> = listOf(
        "&cYou have been temporarily banned!",
        "",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fExpires in: &e%time%",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "",
        "&fYou can appeal at: &9discord.gg/"
    ),

    var ban: List<String> = listOf(
        "&cYou have been banned!",
        "",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fExpires in: &eNever!",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "",
        "&fYou can appeal at: &9discord.gg/"
    ),

    @Comment(
        "This would be the screen a user sees",
        "when they try to join with an alt"
    )
    var evasion: List<String> = listOf(
        "&cAlt Account Detected!",
        "",
        "&fYou cannot join because a linked account (&c%alt%&f) is currently banned.",
        "",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fExpires in: &e%time%",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "",
        "&fYou can appeal at: &9discord.gg/"
    ),

    @Comment("This would send a chat message")
    var warn: List<String> = listOf(
        "&c------------------",
        "",
        "&cYou have been Warned!",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "",
        "&fYou can appeal at: &9discord.gg/",
        "",
        "&c------------------",
    ),

    @Comment("This would send a chat message")
    var tempWarn: List<String> = listOf(
        "&c------------------",
        "&f",
        "&cYou have been &ftemporarily &cWarned!",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fExpires in: &e%time%",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "&f",
        "&fYou can appeal at: &9discord.gg/",
        "&f",
        "&c------------------",
    ),

    @Comment("This would send a chat message")
    var mute: List<String> = listOf(
        "&c------------------",
        "&f",
        "&cYou have been Muted!",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fExpires in: &eNever!",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "&f",
        "&fYou can appeal at: &9discord.gg/",
        "&f",
        "&c------------------",
    ),

    @Comment("This would send a chat message")
    var tempMute: List<String> = listOf(
        "&c------------------",
        "&f",
        "&cYou have been &ftemporarily &cMuted!",
        "&fReason: &e%reason%",
        "&fPunisher: &e%punisher%",
        "&fExpires in: &e%time%",
        "&fPunish date: &e%date%",
        "&fID: &e%id%",
        "&f",
        "&fYou can appeal at: &9discord.gg/",
        "&f",
        "&c------------------",
    )
)


@Configuration
data class PunishmentLogConfig(
    @Comment(
        "If it should send a log message ingame",
        "Only players with the 'sporecore.punishment.notify'",
        "permission will see the logs."
    )
    var shouldLog: Boolean = true,

    @Comment(
        "Format for log messages when a punishment is applied.",
        "Use placeholders:",
        "%user%, %action%, %target%, %reason%, %time%"
    )
    var ban: String = "&9[SporeCore] &f%user% &7has &cbanned &f%target% &7for &e%reason% &7Expires in &e%time%",
    var tempBan: String = "&9[SporeCore] &f%user% &7has &ctemp-banned &f%target% &7for &e%reason% &7Expires in &e%time%",
    var kick: String = "&9[SporeCore] &f%user% &7has &ckicked &f%target% &7for &e%reason%",
    var mute: String = "&9[SporeCore] &f%user% &7has &cmuted &f%target% &7for &e%reason%",
    var tempMute: String = "&9[SporeCore] &f%user% &7has &ctemp-muted &f%target% &7for &e%reason% &7Expires in &e%time%",
    var warn: String = "&9[SporeCore] &f%user% &7has &cwarned &f%target% &7for &e%reason%",
    var tempWarn: String = "&9[SporeCore] &f%user% &7has &ctemp-warned &f%target% &7for &e%reason% &7Expires in &e%time%",

    var unMute: String = "&9[SporeCore] &f%user% &7has &cunmuted &f%target% &7for &e%reason%",
    var unWarn: String = "&9[SporeCore] &f%user% &7has &cunwarn &f%target% &7for &e%reason%",
    var unBan: String = "&9[SporeCore] &f%user% &7has &cunbanned &f%target% &7for &e%reason%",

    @Comment(
        "These are the messages for when someone tries",
        "to talk or join while being banned/muted",
        "Placeholders:",
        "%user%, %reason%, %time%"
    )
    var tryMute: String = "&9[SporeCore] &f%user% &7tried to talk while being &cmuted &7for &e%reason%",
    var tryTempMute: String = "&9[SporeCore] &f%user% &7tried to talk while being &cmuted &7for &e%reason% &c(Expires in: &e%time%)",
    var tryBan: String = "&9[SporeCore] &f%user% &7tried to join while being &cbanned &7for &e%reason%",
    var tryTempBan: String = "&9[SporeCore] &f%user% &7tried to join while being &cbanned &7for &e%reason% &c(Expires in: &e%time%)"
)

@Configuration
data class ReasonsConfig(
    @Comment(
        "Define categories and reasons for punishments.",
        "",
        "Structure example:",
        "cheating:",
        "  xray:",
        "    menu:",
        "      item: DIAMOND_PICKAXE",
        "      name: '&bX-Ray'",
        "      lore:",
        "        - '&fUsing x-ray to find ores.'",
        "    offenses:",
        "      1:",
        "        type: TEMPBAN",
        "        reason: 'X-Ray'",
        "        time: 3d",
        "      2:",
        "        type: BAN",
        "        reason: 'X-Ray (2nd Offense)'"
    )
    var categories: MutableMap<String, MutableMap<String, ReasonDefinition>> = mutableMapOf(
        "cheating" to mutableMapOf(
            "xray" to ReasonDefinition(
                menu = Menu(
                    item = "DIAMOND_PICKAXE",
                    name = "&bX-Ray",
                    lore = listOf(
                        "&fUsing x-ray to find ores.",
                        "&7Examples:",
                        "&e- Ore ESP",
                        "&e- Fullbright Hacks"
                    )
                ),
                offenses = mutableMapOf(
                    1 to ReasonEntry(PunishmentType.TEMPBAN, "X-Ray", "3d"),
                    2 to ReasonEntry(PunishmentType.TEMPBAN, "X-Ray (2nd Offense)", "7d"),
                    3 to ReasonEntry(PunishmentType.BAN, "X-Ray (Final Offense)", null)
                )
            ),
            "base-esp" to ReasonDefinition(
                menu = Menu(
                    item = "SPYGLASS",
                    name = "&eBase ESP",
                    lore = listOf(
                        "&fUsing ESP or tracers to locate bases or players.",
                        "&7Examples:",
                        "&e- Tracers",
                        "&e- Chest ESP"
                    )
                ),
                offenses = mutableMapOf(
                    1 to ReasonEntry(PunishmentType.TEMPBAN, "Base ESP", "5d"),
                    2 to ReasonEntry(PunishmentType.BAN, "Base ESP (2nd Offense)", null)
                )
            )
        ),

        "chat" to mutableMapOf(
            "chat-abuse" to ReasonDefinition(
                menu = Menu(
                    item = "BOOK",
                    name = "&6Chat Abuse",
                    lore = listOf(
                        "&fBeing toxic, rude, or harassing in chat.",
                        "&7Examples:",
                        "&e- Insults",
                        "&e- Slurs",
                        "&e- Excessive toxicity"
                    )
                ),
                offenses = mutableMapOf(
                    1 to ReasonEntry(PunishmentType.TEMPMUTE, "Chat Abuse", "1h"),
                    2 to ReasonEntry(PunishmentType.TEMPMUTE, "Chat Abuse (2nd Offense)", "6h"),
                    3 to ReasonEntry(PunishmentType.TEMPBAN, "Chat Abuse (Final Offense)", "1d")
                )
            ),
            "spamming" to ReasonDefinition(
                menu = Menu(
                    item = "FEATHER",
                    name = "&fSpamming",
                    lore = listOf(
                        "&fFlooding chat or commands.",
                        "&7Examples:",
                        "&e- Message spam",
                        "&e- Caps spam",
                        "&e- Command spam"
                    )
                ),
                offenses = mutableMapOf(
                    1 to ReasonEntry(PunishmentType.MUTE, "Spamming", "15m"),
                    2 to ReasonEntry(PunishmentType.TEMPMUTE, "Spamming (2nd Offense)", "1h"),
                    3 to ReasonEntry(PunishmentType.TEMPMUTE, "Spamming (3rd Offense)", "3h")
                )
            )
        )
    )
)


@Configuration
data class ReasonDefinition(
    var menu: Menu = Menu(),
    var offenses: MutableMap<Int, ReasonEntry> = mutableMapOf()
)

@Configuration
data class ReasonEntry(
    var type: PunishmentType = PunishmentType.WARN,
    var reason: String = "No reason provided",
    var time: String? = null
)

@Configuration
data class Menu(
    var item: String = "PAPER",
    var name: String = "&fUnknown Reason",
    var lore: List<String> = listOf("&7No details provided.")
)
