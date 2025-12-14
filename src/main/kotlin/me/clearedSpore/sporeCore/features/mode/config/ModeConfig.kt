package me.clearedSpore.sporeCore.features.mode.config

import de.exlll.configlib.Comment
import de.exlll.configlib.Configuration
import me.clearedSpore.sporeCore.features.mode.`object`.Mode

@Configuration
data class ModeConfig(
    @Comment(
        "Add your own modes in here. It is highly",
        "recommended to read the wiki page",
        "",
        "To enable a mode you will need the",
        "sporecore.mode.allow and the provided",
        "mode permission.",
        "",
        "For the enable & disable commands",
        "you can use the %player% placeholder.",
        "The commands will be run in console!!",
        "",
        "The command list is a list of commands to",
        "toggle the mode. There is also a /mode|modmode|staffmode",
        "to get the highest available mode and toggle it.",
        "If you add a command that has one of those",
        "aliases it will be blocked from adding it.",
        "",
        "Clear inv will clear the players inventory",
        "and when they leave the mode it will give them",
        "their items back.",
        "",
        "You can run /mode item list",
        "to check all available items."
    )
    var modes: MutableMap<String, Mode> = mutableMapOf(
        "staff" to Mode(
            "Staff",
            "staff",
            1,
            listOf("staffmode", "sm"),
            "sporecore.mode.staff",
            "SURVIVAL",
            true,
            false,
            true,
            false,
            false,
            false,
            false,
            true,
            false,
            listOf("util player sendmessage %player% &cWelcome in Staffmode!"),
            listOf("util player sendmessage %player% &cByee"),
            listOf("invrestore"),
            true,
            true,
            true,
            true,
            mapOf(
                0 to "punish_sword",
                4 to "history",
                8 to "speed"
            )
        ),
        "admin" to Mode(
            "Admin",
            "admin",
            2,
            listOf("adminmode", "am"),
            "sporecore.mode.admin",
            "CREATIVE",
            true,
            true,
            false,
            true,
            true,
            true,
            true,
            true,
            true,
            null,
            null,
            listOf("invrestore"),
            true,
            true,
            true,
            true,
            mapOf(
                0 to "freeze",
                3 to "punish_sword",
                5 to "history",
                8 to "speed"
            )
        )
    )
)
