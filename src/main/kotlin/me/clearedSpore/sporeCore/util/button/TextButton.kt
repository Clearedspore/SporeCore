package me.clearedSpore.sporeCore.util.button

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.CommandSender

class TextButton(private val text: String) {

    private val callbacks = mutableListOf<(CommandSender) -> Unit>()
    private var hover: String? = null
    private var clickEvent: ClickEvent? = null

    fun hoverEvent(text: String): TextButton {
        hover = text
        return this
    }

    fun onClick(fn: (CommandSender) -> Unit): TextButton {
        callbacks.add(fn)
        return this
    }

    fun runCommand(command: String): TextButton {
        clickEvent = ClickEvent.runCommand(command)
        return this
    }

    fun suggestCommand(command: String): TextButton {
        clickEvent = ClickEvent.suggestCommand(command)
        return this
    }

    fun openUrl(url: String): TextButton {
        clickEvent = ClickEvent.openUrl(url)
        return this
    }

    fun copyToClipboard(text: String): TextButton {
        clickEvent = ClickEvent.copyToClipboard(text)
        return this
    }

    fun build(sender: CommandSender): Component {
        val component = LegacyComponentSerializer.legacySection().deserialize(text)

        val finalClickEvent = when {
            callbacks.isNotEmpty() -> {
                val id = CallbackRegistry.register(sender) { player ->
                    callbacks.forEach { it(player) }
                }
                ClickEvent.runCommand("/sporecore callback $id")
            }
            else -> clickEvent
        }

        var finalComponent = finalClickEvent?.let { component.clickEvent(it) } ?: component
        if (hover != null) finalComponent = finalComponent.hoverEvent(HoverEvent.showText(Component.text(hover!!)))
        return finalComponent
    }
}
