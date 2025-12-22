package me.clearedSpore.sporeCore.acf.targets.`object`

import org.bukkit.entity.Entity
import org.bukkit.entity.Player

interface TargetEntities : Collection<Entity>
interface TargetPlayers : Collection<Player>
interface TargetNonPlayers : Collection<Entity>