package me.clearedSpore.sporeCore.user.settings

import me.clearedSpore.sporeCore.util.Perm
import org.bukkit.Material

enum class Setting(
    val key: String,
    val defaultValue: Boolean,
    val displayName: String,
    val item: Material,
    val lore: List<String>,
    val permission: String? = null,
    val configKey: String? = null,
    val skullData: SkullData? = null
) {

    CHAT_ENABLED(
        key = "chat-enabled",
        defaultValue = true,
        displayName = "Chat Enabled",
        item = Material.PAPER,
        lore = listOf(
            "If you can see chat messages sent by players",
            "Staff will bypass this!"
        )
    ),

    TELEPORT_REQUESTS(
        key = "teleport-requests",
        defaultValue = true,
        displayName = "Teleport Requests",
        item = Material.ENDER_EYE,
        lore = listOf(
            "If players can send you teleport requests"
        ),
        configKey = "teleportRequest"
    ),

    PRIVATE_MESSAGES(
        key = "private-msgs",
        defaultValue = true,
        displayName = "Private Messages",
        item = Material.WRITTEN_BOOK,
        lore = listOf(
            "If players can send you private messages",
            "Staff will bypass this!"
        ),
        configKey = "privateMessages"
    ),

    LOGS(
        key = "logs",
        defaultValue = true,
        displayName = "Logs",
        item = Material.HOPPER,
        lore = listOf(
            "If you can see activity logs"
        ),
        permission = Perm.LOG
    ),

    CONFIRM_TPA(
        key = "confirm-tpa",
        defaultValue = true,
        displayName = "Confirm TPA",
        item = Material.PLAYER_HEAD,
        lore = listOf(
            "When accepting & sending a TPA/TPAhere",
            "should it open the confirmation menu?"
        ),
        skullData = SkullData(
            name = "skinf5d83348",
            texture = "ewogICJ0aW1lc3RhbXAiIDogMTYyMzQ5NzM5Mjg3NCwKICAicHJvZmlsZUlkIiA6ICI0NWY3YTJlNjE3ODE0YjJjODAwODM5MmRmN2IzNWY0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfSnVzdERvSXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGE5OWIwNWI5YTFkYjRkMjliNWU2NzNkNzdhZTU0YTc3ZWFiNjY4MTg1ODYwMzVjOGEyMDA1YWViODEwNjAyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
            signature = "MfT0XIaVibQXCkQGoZu6vAXHrwfcGSQhoJZCQHfrhqbgoluQ8QmZMUtyOp6Wmgk+7suHaXXlTkYlcM4xlOh93edqAl2AR8uBuQIRzrMWRhTEXmD68mLGQbSJ7N2lMWoTjxM6posfaPhopCelYZAYxpm58xW9cp5Ua492yPjyELoj9DgK8MzUX/PFmB1v6k3PQ6Asyjvt1xEUyP4utUrIIkNCfrF5ujvVyiIZkgv8JXU5bVf7JC6hM4h+xvx/F1z/a6sn7nnOJ8xWVxCP6yJNQ31cXv/Za/FR3i8xK2ODdHigdgvSFycoyrCk8WSMT3TvNurTnTBKHwd8F59wIErWQQIdEPlu2E3hSPg4KOYREMn6972mwuB4Nov5wccgS0bk2VOBuq7Z3HI+rzk7d0IN8FY1wcSeQcX7FmIVvaksbdOgL8h6FJo2NY3XqYKo+3PW9nWcAiLBckSgeDHQEAlqtqzWLFbwkRL2XfMX91qUUL1rPEpgdsrdHoHEEYNNWyR3z6MENcNUAFb2W0ABpjtxJoOhsEGkw81As+t6hdvthhLbsEU0lS9I5dEh7Kzwo44ngtLFcO2FpvQORKFg55cHE6Hajz9JZ05nYf8PvHWTZQmj/agqiPwrvsks/+Offv8Y2DzZaWqVp6XXK9h11OVum8vOXqjPKCkiDC2J0zVgw8Q="
        ),
        configKey = "teleportRequest"
    ),

    CURRENCY_LOGS(
        key = "currency-logs",
        defaultValue = true,
        displayName = "Currency Logs",
        item = Material.HOPPER,
        lore = listOf(
            "If you can see the custom currency logs."
        ),
        permission = Perm.CURRENCY_NOTIFY,
        configKey = "currency.enabled"
    )

    ;
}
