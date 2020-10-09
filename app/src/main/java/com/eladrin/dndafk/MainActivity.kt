package com.eladrin.dndafk

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postDelayed
import coil.load
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.character_token.view.*
import java.util.*

fun generateGoblin(number: Int, isFrontRow: Boolean) = Creature(
    name = "Goblin$number",
    imageUrl = "https://media-waterdeep.cursecdn.com/avatars/thumbnails/0/351/218/315/636252777818652432.jpeg",
    maxHitPoints = 7,
    armorClass = 15,
    weapon = Weapon("Scimitar", 4) { d(6) + 2 },
    isFrontRow = isFrontRow
)

val allies = listOf(
    Creature(
        name = "TON",
        imageUrl = "https://www.dndbeyond.com/avatars/9221/828/637202355879380101.jpeg",
        maxHitPoints = 21,
        armorClass = 16,
        weapon = Weapon("Crossbow, Light", 5) { d(8) + 3 },
        isFrontRow = false
    ),
    Creature(
        name = "Meladrin Elden",
        imageUrl = "https://www.dndbeyond.com/avatars/11284/327/1581111423-31046859.jpeg",
        maxHitPoints = 15,
        armorClass = 12,
        weapon = Weapon("Fire Bolt", 5) { d(10) },
        isFrontRow = false
    ),
    Creature(
        name = "Phirina Ophinshtalajiir",
        imageUrl = "https://www.dndbeyond.com/avatars/9107/482/637196228800489268.jpeg",
        maxHitPoints = 20,
        armorClass = 18,
        weapon = Weapon("Longsword, +1", 6) { d(8) + 4 },
        isFrontRow = true
    ),
)

val enemies = listOf(
    Creature(
        name = "Owlbear",
        imageUrl = "https://media-waterdeep.cursecdn.com/avatars/thumbnails/0/315/256/315/636252772225295187.jpeg",
        maxHitPoints = 59,
        armorClass = 13,
        weapon = Weapon("Beak", 7) { d(10) + 5 },
        isFrontRow = true
    ),
    generateGoblin(1, false),
    generateGoblin(2, false),
)

fun allCharacters() = allies.plus(enemies)

class MainActivity : AppCompatActivity() {

    fun allyBackLine() = listOf(token1, token2, token3, token4)
    fun allyFrontLine() = listOf(token5, token6, token7, token8)
    fun allyViews() = allyBackLine().plus(allyFrontLine())

    fun enemyBackLine() = listOf(token9, token10, token11, token12)
    fun enemyFrontLine() = listOf(token13, token14, token15, token16)
    fun enemyViews() = enemyBackLine().plus(enemyFrontLine())

    fun allViews() = allyViews().plus(enemyViews())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        assignCharactersToViews()
        play.setOnClickListener {
            reset()
            round(1, 100)
        }
    }

    private fun assignCharactersToViews() {
        allViews().forEach { it.visibility = View.GONE }
        allies.forEach { creature ->
            val line = if (creature.isFrontRow) allyFrontLine() else allyBackLine()
            line.filter { it.tag == null }[0].apply {
                bind(creature)
                tag = creature
                visibility = View.VISIBLE
            }
        }
        enemies.forEach { creature ->
            val line = if (creature.isFrontRow) enemyFrontLine() else enemyBackLine()
            line.filter { it.tag == null }[0].apply {
                bind(creature)
                tag = creature
                visibility = View.VISIBLE
            }
        }
    }

    private fun reset() {
        allCharacters().forEach { creature ->
            creature.reset()
            findCreatureView(creature)?.apply {
                bind(creature)
                alpha = 1f
            }
        }
    }

    private fun round(number: Int, roundTime: Long) {
        currentAction.text = "Round $number"
        currentAction.postDelayed(roundTime) {
            teamAttack(alliesAlive(), enemiesAlive(), roundTime) {
                when {
                    alliesAlive().isEmpty() -> currentAction.text = "Enemies won"
                    enemiesAlive().isEmpty() -> currentAction.text = "You won"
                    else -> teamAttack(enemiesAlive(), alliesAlive(), roundTime) {
                        when {
                            alliesAlive().isEmpty() -> currentAction.text = "Enemies won"
                            enemiesAlive().isEmpty() -> currentAction.text = "You won"
                            else -> round(number + 1, roundTime)
                        }
                    }
                }
            }
        }
    }

    fun alliesAlive() = allies.filter { it.alive }.toList()
    fun enemiesAlive() = enemies.filter { it.alive }.toList()

    private fun teamAttack(
        team: List<Creature>,
        opponents: List<Creature>,
        roundTime: Long,
        callback: () -> Unit
    ) {
        if (team.isEmpty() || opponents.isEmpty()) return
        // hero attacks
        val hero = team[0]
        val enemy = opponents.random()
        currentAction.text = hero.attack(enemy)
        Log.d("qwerty", currentAction.text.toString())
        // update enemy
        val enemyView = findCreatureView(enemy)
        enemyView?.bind(enemy)
        // update teams
        val remainingTeam = team.minus(hero)
        val remainingOpponents = opponents.filter { it.alive }
        // next calls
        currentAction.postDelayed(roundTime) {
            if (remainingTeam.isEmpty() || remainingOpponents.isEmpty()) {
                // all team members attacked, return
                callback()
            } else {
                // there are some opponents alive
                // next team member attacks
                teamAttack(remainingTeam, remainingOpponents, roundTime, callback)
            }
        }
    }

    private fun findCreatureView(creature: Creature) = allViews().find { it.tag == creature }
}

class CharacterToken @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.character_token, this)
    }

    fun bind(creature: Creature) {
        name.text = creature.name
        image.load(creature.imageUrl)
        hit_points.text = "HP: ${creature.hitPoints}/${creature.maxHitPoints}"
        animate().alpha(if (creature.alive) 1f else 0f).start()
    }
}

class Creature(
    val name: String,
    val imageUrl: String,
    val maxHitPoints: Int,
    var hitPoints: Int = maxHitPoints,
    val armorClass: Int,
    val weapon: Weapon,
    val isFrontRow: Boolean
) {
    val alive: Boolean
        get() = hitPoints > 0

    fun attack(target: Creature): String {
        val hit = d(20) + weapon.hit
        if (hit >= target.armorClass) {
            val damage = weapon.damage()
            target.hitPoints -= damage
            if (target.hitPoints <= 0) {
                target.hitPoints = 0
                return "$name damaged $damage and killed ${target.name}"
            }
            return "$name damaged $damage ${target.name}"
        }
        return "$name missed ${target.name}"
    }

    fun reset() {
        hitPoints = maxHitPoints
    }
}

data class Weapon(
    val name: String,
    val hit: Int,
    val damage: () -> Int
)

fun d(die: Int) = randomInt(die) + 1

fun <T> Collection<T>.random() = toList()[randomInt(size)]

fun randomInt(max: Int) = Random().nextInt(max)

fun randomBoolean() = randomInt(2) == 1