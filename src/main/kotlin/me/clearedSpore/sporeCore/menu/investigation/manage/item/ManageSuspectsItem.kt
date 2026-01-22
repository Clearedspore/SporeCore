package me.clearedSpore.sporeCore.menu.investigation.manage.item

import me.clearedSpore.sporeAPI.menu.Item
import me.clearedSpore.sporeAPI.util.CC.blue
import me.clearedSpore.sporeCore.features.investigation.IGService
import me.clearedSpore.sporeCore.features.investigation.`object`.Investigation
import me.clearedSpore.sporeCore.menu.investigation.manage.suspect.SuspectMenu
import me.clearedSpore.sporeCore.util.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class ManageSuspectsItem(
    val investigationID: String,
) : Item() {

    override fun createItem(): ItemStack {
        val investigation = IGService.findInvestigation(investigationID)!!
        return ItemBuilder(Material.PLAYER_HEAD)
            .setSkullTexture(
                "GGl5BrK+AI6XfnGfj5P3/eYbJ25jTeBBK8kCCaXyj1AJv0YGv0w+5qsTrEPfAI6n4wfxqOaT6K1jYEjmFlVGbZVvbc0smXRNltBd6zB7XY5fArpNuuLOErw1pvRDE+xy8xzuKdlvv5sU99RzVYJEPI8CuyyZvW/lTOAaETtB2iedJQgDouLM/ptvRtDHnDe1cYIMOFkQGdCdxoRMokNptyX17CO9gUdjuJ7fOez6+xj0rN5uSnB3up/wDWJRCex9GjLflCi16Z9yW6Hkr06xbPITG6h7XBBp/G2dqtER1STgWWPZIhXZ18npJ1jdnP8TdT716hc5SCruxMgww8teFWdU+RChBMikreTlgkUWbOgHkCU0biyTCA9B+OjmJBWsG2M2Y1KFD1ZADMl007hASAsnSrr22vPYb6UdXPFrS0z6WxGzDQy8YouBs3QiFPG3nFub10b11jibJEicsrLxT+dsseZmnabgJmOxYWS/VjcLrwkVf/aLEx1wC2ciygDc27OMp9juTTnN46t2HA7hq/7x7sY/og4/XQ3kT0o2PkJaz0XLO1RLtvLJN2kZI8yAXhZHIUZ2mgEBeHmCqVfL7BoMdUy3XISUrTCxCd83NWwepLMe8opH+a8TlZRmOgyA4x+vssyCKfX7ltR6A03gr6EbuAR16r8AQsb3d7tVAKQ=",
                "ewogICJ0aW1lc3RhbXAiIDogMTc2Nzk2NDkwMTM5NCwKICAicHJvZmlsZUlkIiA6ICI0NDAzZGM1NDc1YmM0YjE1YTU0OGNmZGE2YjBlYjdkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICI4Qml0czFCeXRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgwZTk1YWM5ODFiNGFkMGM5ZDMzNWQyMmVmMTljZDllMDNiNDRjZmNhYzY0OTQ0ZDJjMDhjY2QxMWFmMWQ0OTgiCiAgICB9LAogICAgIkNBUEUiIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIzNDBjMGUwM2RkMjRhMTFiMTVhOGIzM2MyYTdlOWUzMmFiYjIwNTFiMjQ4MWQwYmE3ZGVmZDYzNWNhN2E5MzMiCiAgICB9CiAgfQp9"
            )
            .setName("Manage suspects".blue())
            .addLoreLine("Suspects: ${investigation.suspects.size}".blue())
            .addUsageLine(ClickType.LEFT, "manage suspects")
            .build()
    }

    override fun onClickEvent(clicker: Player, clickType: ClickType) {
        SuspectMenu(investigationID, clicker).open(clicker)
    }
}