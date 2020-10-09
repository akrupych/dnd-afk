package com.eladrin.dndafk

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postDelayed
import coil.load
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.character_token.view.*
import java.util.*

val racenar = Creature(
    name = "Racenar",
    imageUrl = "https://www.dndbeyond.com/avatars/10/86/636339381849352911.png",
    maxHitPoints = 150,
    armorClass = 15,
    weapon = Weapon("Greataxe", 5) { d(12) + 3 }
)

fun generateGoblin() = Creature(
    name = "Goblin${random(20)}",
    imageUrl = "https://media-waterdeep.cursecdn.com/avatars/thumbnails/0/351/218/315/636252777818652432.jpeg",
    maxHitPoints = 7,
    armorClass = 15,
    weapon = Weapon("Scimitar", 4) { d(6) + 2 }
)

class MainActivity : AppCompatActivity() {

    val allies by lazy {
        mapOf(
            token1 to racenar
        )
    }

    val enemies by lazy {
        mapOf(
            token2 to generateGoblin(),
            token3 to generateGoblin()
        )
    }

    val allEntries: List<Pair<CharacterToken, Creature>>
    get() = allies.plus(enemies).toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        reset()
        play.setOnClickListener {
            reset()
            round(1, 2000)
        }
    }

    private fun reset() {
        allEntries.forEach { (view, creature) ->
            creature.reset()
            view.bind(creature)
            view.alpha = 1f
        }
    }

    private fun round(number: Int, roundTime: Long) {
        currentAction.text = "Round $number"
        currentAction.postDelayed(roundTime) {
//            currentAction.text = racenar.attack(goblin)
//            allies.forEach { view, ally ->
//                val enemy = enemies.values.random()
//                currentAction.text = ally.attack(enemy)
//            }
//            token2.bind(goblin)
//            if (goblin.alive) currentAction.postDelayed(roundTime) {
//                currentAction.text = goblin.attack(racenar)
//                token1.bind(racenar)
//                if (racenar.alive) {
//                    currentAction.postDelayed(roundTime) { round(number + 1, roundTime) }
//                }
//            }
            teamAttack(alliesAlive(), enemiesAlive(), roundTime) {
                teamAttack(enemiesAlive(), alliesAlive(), roundTime) {
                    when {
                        alliesAlive().isEmpty() -> currentAction.text = "Enemies won"
                        enemiesAlive().isEmpty() -> currentAction.text = "You won"
                        else -> round(number + 1, roundTime)
                    }
                }
            }
        }
    }

    fun alliesAlive() = allies.values.filter { it.alive }.toList()
    fun enemiesAlive() = enemies.values.filter { it.alive }.toList()

    private fun teamAttack(team: List<Creature>, opponents: List<Creature>, roundTime: Long, callback: () -> Unit) {
        if (team.isEmpty() || opponents.isEmpty()) return
        // hero attacks
        val hero = team[0]
        val enemy = opponents.random()
        currentAction.text = hero.attack(enemy)
        // update enemy
        val enemyView = findCreatureView(enemy)
        enemyView?.bind(enemy)
        // update teams
        val remainingTeam = team.minus(hero)
        val remainingOpponents = opponents.filter { it.alive }
        // next calls
        currentAction.postDelayed(roundTime) {
            if (remainingTeam.isEmpty()) {
                // all team members attacked, return
                callback()
            } else if (opponents.isNotEmpty()) {
                // there are some opponents alive
                // next team member attacks
                teamAttack(remainingTeam, remainingOpponents, roundTime, callback)
            }
        }
    }

    private fun findCreatureView(creature: Creature) = allEntries.find { it.second == creature }?.first
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
    val weapon: Weapon
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
            return "$name damaged $damage"
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

fun d(die: Int) = random(die) + 1

fun <T> Collection<T>.random() = toList()[random(size)]

fun random(max: Int) = Random().nextInt(max)