package me.clearedSpore.sporeCore.acf.targets.impl

import me.clearedSpore.sporeCore.acf.targets.`object`.TargetEntities
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetNonPlayers
import me.clearedSpore.sporeCore.acf.targets.`object`.TargetPlayers
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class TargetPlayersImpl(
    private val players: List<Player>
) : TargetPlayers, Collection<Player> by players

class TargetEntitiesImpl(
    private val entities: List<Entity>
) : TargetEntities, Collection<Entity> by entities

class TargetNonPlayersImpl(
    private val entities: List<Entity>
) : TargetNonPlayers, Collection<Entity> by entities
