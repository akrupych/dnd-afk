package com.eladrin.dndafk.character

import java.util.*

data class Die(val number: Int) {
    fun roll(): Int = Random().nextInt(number) + 1
}

data class DicePool(val dice: List<Die>) {

    private var availableDice = dice.toMutableList()
    private var usedDice = mutableListOf<Die>()

    fun roll(die: Die): Int {
        if (availableDice.remove(die)) usedDice.add(die)
        return die.roll()
    }

    fun reset() {
        availableDice = dice.toMutableList()
        usedDice = mutableListOf()
    }
}