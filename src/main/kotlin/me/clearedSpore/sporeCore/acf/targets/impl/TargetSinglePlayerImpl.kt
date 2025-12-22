package me.clearedSpore.sporeCore.acf.targets.impl

import me.clearedSpore.sporeCore.acf.targets.`object`.TargetSinglePlayer
import org.bukkit.entity.Player

class TargetSinglePlayerImpl(
    override val player: Player
) : TargetSinglePlayer
