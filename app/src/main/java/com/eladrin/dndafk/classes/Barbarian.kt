package com.eladrin.dndafk.classes

import com.eladrin.dndafk.character.Ability
import com.eladrin.dndafk.character.Die

class Barbarian : DndClass() {

    override val die = Die(12)

    override val saveProficiencies = listOf(
        Ability.STRENGTH,
        Ability.CONSTITUTION,
    )
}