package com.eladrin.dndafk.character

data class Health(
    var maximum: Int,
    var current: Int = maximum,
    var temporary: Int = 0,
    val hitDice: DicePool
)