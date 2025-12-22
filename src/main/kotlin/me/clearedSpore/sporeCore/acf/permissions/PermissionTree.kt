package me.clearedSpore.sporeCore.acf.permissions

class PermissionTree(
    permissions: Collection<String>
) {
    private val children: Map<String, Set<String>>

    init {
        val map = mutableMapOf<String, MutableSet<String>>()

        permissions.map { it.lowercase() }.forEach { perm ->
            val parts = perm.split(".")
            for (i in 1..parts.size) {
                val parent = parts.take(i - 1).joinToString(".")
                val child = parts.take(i).joinToString(".")
                map.computeIfAbsent(parent) { mutableSetOf() }.add(child)
            }
        }

        children = map
    }

    fun complete(input: String): List<String> {
        val parts = input.split(".")
        val parent = parts.dropLast(1).joinToString(".")
        return children[parent].orEmpty().sorted()
    }
}
