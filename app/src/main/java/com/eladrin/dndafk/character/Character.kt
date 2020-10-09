package com.eladrin.dndafk.character

import com.eladrin.dndafk.classes.DndClass
import com.eladrin.dndafk.races.Race

data class Character(
    var name: String,
    var race: Race,
    var dndClass: DndClass,
    var experience: Int = 0,
    var strength: Int = 8,
    var dexterity: Int = 8,
    var constitution: Int = 8,
    var intelligence: Int = 8,
    var wisdom: Int = 8,
    var charisma: Int = 8,
) {

    val level: Int
        get() = when {
            experience < 300 -> 1
            experience < 900 -> 2
            experience < 2700 -> 3
            experience < 6500 -> 4
            experience < 14000 -> 5
            experience < 23000 -> 6
            experience < 34000 -> 7
            experience < 48000 -> 8
            experience < 64000 -> 9
            experience < 85000 -> 10
            experience < 100000 -> 11
            experience < 120000 -> 12
            experience < 140000 -> 13
            experience < 165000 -> 14
            experience < 195000 -> 15
            experience < 225000 -> 16
            experience < 265000 -> 17
            experience < 305000 -> 18
            experience < 355000 -> 19
            else -> 20
        }

    val proficiencyBonus: Int
        get() = when {
            level < 5 -> 2
            level < 9 -> 3
            level < 13 -> 4
            level < 17 -> 5
            else -> 6
        }

    var speed: Int = race.speed

    var inspiration: Boolean = false

    val health = Health(
        maximum = dndClass.die.number + constitution.modifier * level,
        hitDice = DicePool(List(level) { dndClass.die })
    )

    fun saveModifier(ability: Ability): Int = ability.score.modifier +
            if (dndClass.saveProficiencies.contains(ability)) proficiencyBonus else 0

    var initiative: Int = dexterity.modifier

    var armorClass: Int = 10 + dexterity.modifier // + equipment.armor

    val Int.modifier: Int
        get() = (this - 10) / 2

    val Ability.score: Int
        get() = when (this) {
            Ability.STRENGTH -> strength
            Ability.DEXTERITY -> dexterity
            Ability.CONSTITUTION -> constitution
            Ability.INTELLIGENCE -> intelligence
            Ability.WISDOM -> wisdom
            Ability.CHARISMA -> charisma
        }
}