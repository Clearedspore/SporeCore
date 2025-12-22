package me.clearedSpore.sporeCore.acf

import co.aikar.commands.BukkitCommandExecutionContext
import co.aikar.commands.contexts.ContextResolver
import me.clearedSpore.sporeAPI.util.Message.sendErrorMessage
import me.clearedSpore.sporeCore.acf.error.SelectorErrorHandler
import me.clearedSpore.sporeCore.acf.permissions.PermissionTree
import me.clearedSpore.sporeCore.acf.selector.SelectorMeta
import me.clearedSpore.sporeCore.acf.selector.SortType
import me.clearedSpore.sporeCore.acf.selector.parseDoubleRange
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

enum class TargetType {
    ALL, PLAYERS_ONLY, ENTITIES_ONLY
}

class TargetSelectorResolver(
    private val targetType: TargetType = TargetType.ALL
) : ContextResolver<Collection<Entity>, BukkitCommandExecutionContext> {

    private val allPermissions = Bukkit.getPluginManager().permissions.map { it.name }
    private val permissionTree = PermissionTree(allPermissions)

    override fun getContext(context: BukkitCommandExecutionContext): Collection<Entity> {
        val sender = context.sender
        val input = context.popFirstArg() ?: run {
            sender.sendErrorMessage("You must specify a target.")
            return emptyList()
        }

        val meta = parseSelectorMeta(input)
        val resolved = resolve(input, context.player)

        if (resolved.isEmpty()) {
            when (targetType) {
                TargetType.PLAYERS_ONLY -> SelectorErrorHandler.sendNoPlayerFound(sender, input)
                TargetType.ENTITIES_ONLY -> SelectorErrorHandler.sendOnlyEntitiesAllowed(sender, input)
                TargetType.ALL -> SelectorErrorHandler.sendNoEntityFound(sender, input)
            }
            return emptyList()
        }

        val filtered = when (targetType) {
            TargetType.ALL -> resolved
            TargetType.PLAYERS_ONLY -> resolved.filterIsInstance<Player>()
            TargetType.ENTITIES_ONLY -> resolved.filter { it !is Player }
        }

        if (targetType == TargetType.PLAYERS_ONLY) {
            meta?.arguments?.get("type")?.let { type ->
                if (type.lowercase() != "player") {
                    SelectorErrorHandler.sendOnlyPlayersAllowed(sender, input)
                    return emptyList()
                }
            }
        }

        if (filtered.isEmpty()) {
            when (targetType) {
                TargetType.PLAYERS_ONLY -> SelectorErrorHandler.sendNoPlayerFound(sender, input)
                TargetType.ENTITIES_ONLY -> SelectorErrorHandler.sendOnlyEntitiesAllowed(sender, input)
                TargetType.ALL -> SelectorErrorHandler.sendNoEntityFound(sender, input)
            }
            return emptyList()
        }

        return filtered
    }

    fun parseSelectorMeta(input: String): SelectorMeta? {
        if (!input.startsWith("@")) return null
        val selector = input.take(2)
        val args =
            if (input.contains("[") && input.endsWith("]")) {
                input.substringAfter("[").substringBeforeLast("]")
                    .split(",").mapNotNull {
                        val parts = it.split("=")
                        if (parts.size == 2) parts[0].lowercase() to parts[1].lowercase() else null
                    }.toMap()
            } else emptyMap()
        return SelectorMeta(selector, args)
    }

    private fun resolve(value: String, sender: Player?): List<Entity> {
        val lower = value.lowercase()
        return when (lower) {
            "@a" -> Bukkit.getOnlinePlayers().toList()
            "@s" -> listOfNotNull(sender)
            "@p" -> sender?.let { listOfNotNull(nearestPlayer(it)) } ?: emptyList()
            "@r" -> Bukkit.getOnlinePlayers().shuffled().take(1)
            else -> {
                if (lower.startsWith("@e[")) {
                    val inside = value.substringAfter("[").substringBeforeLast("]")
                    parseSelector(inside, sender)
                } else {
                    Bukkit.getPlayerExact(value)?.let { listOf(it) } ?: emptyList()
                }
            }
        }
    }

    private fun nearestPlayer(sender: Player): Player? {
        return Bukkit.getOnlinePlayers()
            .filter { it != sender }
            .minByOrNull { it.location.distanceSquared(sender.location) }
    }

    private fun parseSelector(argsStr: String, sender: Player?): List<Entity> {
        var candidates = Bukkit.getWorlds().flatMap { it.entities }.toMutableList()
        if (targetType == TargetType.PLAYERS_ONLY) {
            candidates = candidates.filterIsInstance<Player>().toMutableList()
        }

        val filters = argsStr.split(",").mapNotNull { entry ->
            val index = entry.indexOf("=")
            if (index == -1) null else entry.substring(0, index).lowercase() to entry.substring(index + 1)
        }.toMap()

        val sortType = filters["sort"]?.uppercase()?.let { runCatching { SortType.valueOf(it) }.getOrNull() }

        filters.forEach { (key, value) ->
            val negated = value.isNegated()
            val raw = value.raw()

            when (key) {
                "gamemode" -> {
                    val mode = GameMode.values().firstOrNull { it.name.equals(raw, true) } ?: return@forEach
                    candidates =
                        candidates.filter { it is Player && if (negated) it.gameMode != mode else it.gameMode == mode }
                            .toMutableList()
                }

                "name" -> {
                    candidates = candidates.filter {
                        it is Player && if (negated) !it.name.equals(raw, true) else it.name.equals(
                            raw,
                            true
                        )
                    }.toMutableList()
                }

                "distance" -> {
                    if (sender != null) {
                        val range = parseDoubleRange(raw) ?: return@forEach
                        candidates = candidates.filter {
                            it is Player && it.world == sender.world &&
                                    if (negated) !range.contains(it.location.distance(sender.location))
                                    else range.contains(it.location.distance(sender.location))
                        }.toMutableList()
                    }
                }

                "tag" -> {
                    candidates = candidates.filter {
                        if (negated) !it.scoreboardTags.contains(raw) else it.scoreboardTags.contains(raw)
                    }.toMutableList()
                }

                "type" -> {
                    val type = EntityType.values().firstOrNull { it.name.equals(raw, true) } ?: return@forEach
                    candidates = candidates.filter { if (negated) it.type != type else it.type == type }.toMutableList()
                }

                "haspermission" -> {
                    candidates = candidates.filter {
                        it is Player && if (negated) !it.hasPermission(raw) else it.hasPermission(raw)
                    }.toMutableList()
                }

                "world" -> {
                    val world = Bukkit.getWorld(raw) ?: return@forEach
                    candidates =
                        candidates.filter { if (negated) it.world != world else it.world == world }.toMutableList()
                }

                "limit" -> {
                    val limit = raw.toIntOrNull() ?: return@forEach
                    if (limit < candidates.size) candidates = candidates.take(limit).toMutableList()
                }
            }
        }

        candidates = sortEntities(candidates, sender, sortType)

        candidates = when (targetType) {
            TargetType.ALL -> candidates
            TargetType.PLAYERS_ONLY -> candidates.filterIsInstance<Player>().toMutableList()
            TargetType.ENTITIES_ONLY -> candidates.filter { it !is Player }.toMutableList()
        }

        return candidates
    }


    fun getTabCompletions(partial: String): List<String> {
        val completions = mutableListOf<String>()
        val lower = partial.lowercase()

        if (lower.isEmpty()) {
            completions.addAll(listOf("@p", "@s", "@r", "@a", "@e"))
            completions.addAll(Bukkit.getOnlinePlayers().map { it.name })
            return completions
        }

        if ("@a".startsWith(lower)) completions.add("@a")
        if ("@p".startsWith(lower)) completions.add("@p")
        if ("@s".startsWith(lower)) completions.add("@s")
        if ("@r".startsWith(lower)) completions.add("@r")
        if ("@e".startsWith(lower) && lower != "@e[") completions.add("@e")

        if (lower.startsWith("@e[")) {
            val inside = lower.substringAfter("[")
            val parts = inside.split(",")
            val lastPart = parts.last()
            val previousParts = parts.dropLast(1)

            val selectorKeys = listOf(
                "type=", "gamemode=", "name=", "distance=", "tag=", "haspermission=",
                "world=", "limit=", "sort="
            )

            selectorKeys.forEach { key ->
                if (key.startsWith(lastPart)) {
                    completions.add("@e[${(previousParts + key).joinToString(",")}")
                }
            }


            selectorKeys.forEach { key ->
                if (lastPart.startsWith(key)) {
                    val valuePartial = lastPart.removePrefix(key)
                    when (key) {
                        "gamemode=" -> GameMode.values()
                            .map { it.name.lowercase() }
                            .filter { it.startsWith(valuePartial) }
                            .forEach {
                                completions.add("@e[${(previousParts + "$key$it").joinToString(",")}")
                            }

                        "name=" -> Bukkit.getOnlinePlayers()
                            .map { it.name.lowercase() }
                            .filter { it.startsWith(valuePartial) }
                            .forEach { completions.add("@e[${(previousParts + "$key$it").joinToString(",")}") }

                        "type=" -> EntityType.values()
                            .map { it.name.lowercase() }
                            .filter { it.startsWith(valuePartial) }
                            .forEach { completions.add("@e[${(previousParts + "$key$it").joinToString(",")}") }

                        "distance=" -> listOf("5", "5..", "..5", "5..10")
                            .filter { it.startsWith(valuePartial) }
                            .forEach { completions.add("@e[${(previousParts + "$key$it").joinToString(",")}") }

                        "haspermission=" -> {
                            val negated = valuePartial.startsWith("!")
                            val raw = valuePartial.removePrefix("!")
                            val suggestions = permissionTree.complete(raw)
                            suggestions
                                .filter { it.startsWith(raw) }
                                .forEach {
                                    val value = if (negated) "!$it" else it
                                    completions.add("@e[${(previousParts + "$key$value").joinToString(",")}")
                                }
                            if (!negated && raw.isNotEmpty()) {
                                suggestions
                                    .filter { it.startsWith(raw) }
                                    .forEach { completions.add("@e[${(previousParts + "$key!$it").joinToString(",")}") }
                            }
                        }

                        "sort=" -> SortType.values()
                            .map { it.name.lowercase() }
                            .filter { it.startsWith(valuePartial) }
                            .forEach { completions.add("@e[${(previousParts + "$key$it").joinToString(",")}") }

                        "world=" -> Bukkit.getWorlds()
                            .map { it.name.lowercase() }
                            .filter { it.startsWith(valuePartial) }
                            .forEach {
                                completions.add("@e[${(previousParts + "$key$it").joinToString(",")}")
                            }

                        "limit=" -> (1..99)
                            .map { it.toString() }
                            .filter { it.startsWith(valuePartial) }
                            .forEach { completions.add("@e[${(previousParts + "$key$it").joinToString(",")}") }
                    }
                }
            }
        }

        Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { it.lowercase().startsWith(lower) }
            .forEach { completions.add(it) }

        return completions
            .distinct()
            .sortedWith(compareBy(::selectorPriority).thenBy { it })
    }

    private fun selectorPriority(value: String): Int = when {
        value.startsWith("@p") -> 0
        value.startsWith("@s") -> 1
        value.startsWith("@r") -> 2
        value.startsWith("@a") -> 3
        value.startsWith("@e") -> 4
        else -> 5
    }

    private fun sortEntities(entities: MutableList<Entity>, sender: Player?, sort: SortType?): MutableList<Entity> {
        if (sort == null || sender == null) return entities
        return when (sort) {
            SortType.NEAREST -> entities.sortedBy { it.location.distanceSquared(sender.location) }
            SortType.FURTHEST -> entities.sortedByDescending { it.location.distanceSquared(sender.location) }
            SortType.RANDOM -> entities.shuffled()
            SortType.NORTH -> entities.sortedByDescending { it.location.z }
            SortType.SOUTH -> entities.sortedBy { it.location.z }
            SortType.EAST -> entities.sortedByDescending { it.location.x }
            SortType.WEST -> entities.sortedBy { it.location.x }
            SortType.UP -> entities.sortedByDescending { it.location.y }
            SortType.DOWN -> entities.sortedBy { it.location.y }
        }.toMutableList()
    }

    private fun String.isNegated(): Boolean = startsWith("!")
    private fun String.raw(): String = removePrefix("!")
}
