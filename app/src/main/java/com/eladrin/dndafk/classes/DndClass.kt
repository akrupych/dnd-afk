package com.eladrin.dndafk.classes

import com.eladrin.dndafk.character.Ability
import com.eladrin.dndafk.character.Die

abstract class DndClass {

    abstract val die: Die

    abstract val saveProficiencies: List<Ability>
}