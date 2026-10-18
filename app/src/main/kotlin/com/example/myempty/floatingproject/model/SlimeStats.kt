package com.example.myempty.floatingproject.model

import kotlinx.serialization.Serializable

@Serializable
data class SlimeStats(
    val hunger: Int = 100,      // 0..100 (100 = full)
    val happiness: Int = 100,   // 0..100
    val energy: Int = 100,      // 0..100
    val level: Int = 1
) {
    fun copyWithHungerDelta(delta: Int): SlimeStats {
        val new = (hunger + delta).coerceIn(0, 100)
        return copy(hunger = new)
    }
}
