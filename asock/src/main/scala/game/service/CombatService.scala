package game.service

import core.app.Component
import game.entity.HasStats
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import scala.math._
import game.entity.ui.LogWindow
import scala.util.Random
import core.utils.MathUtil

case class DealDamage(ammount: Int)

class CombatService extends Component {
  val log = injectActor[LogWindow]
  val r = new Random

  def meeleHit(attacker: HasStats with DynamicEntity, defender: HasStats with DynamicEntity) = {
    defender.current.meeleDefense.value - attacker.current.meeleDmg.value match {
      case dmg if dmg < 0 =>
        println("dmg")
        defender.self ! DealDamage(-dmg)
      case dmg if dmg >= 0 => println(dmg) // TODO: do some diminishing returnish something
    }
  }

  def computeHit(attacker: HasStats with Entity, defender: HasStats with Entity) = {
    val dist = getDistance(attacker, defender)
    if (dist > attacker.current.range.value) // completely out of range
      0
    else {
      val currentHit = 100 - attacker.current.hitPenalty.value
      if (attacker.current.movement.value < attacker.max.movement.value) { // I moved
        // apply the movement penalty
        currentHit - attacker.max.movementPenalty.value // TODO: make this come from current (so I can have debufs that do movementPenalty
      } else {
        currentHit 
      }
    }
  }

  def getDistance(attacker: HasStats with Entity, defender: HasStats with Entity) = {
    MathUtil.distance(attacker, defender)
  }

  def computeRangedDmg(attacker: HasStats, defender: HasStats) = defender.current.rangedDefense.value - attacker.current.rangedDmg.value

  def rangedHit(attacker: HasStats with DynamicEntity, defender: HasStats with DynamicEntity) = {
    // check range
    val hit = computeHit(attacker, defender)
    hit match {
      case n if n == 0 => log.add("player misses")
      case _ =>
        val hitRoll = r.nextInt(100)
        hitRoll match {
          case r if r > hit => log.add("player misses") // missed
          case _ => // player hit so, invoke damage
            computeRangedDmg(attacker, defender) match { 
              case dmg if dmg < 0 =>
                log.add("player hits for " + (-dmg))
                defender.self ! DealDamage(-dmg)
              case dmg if dmg >= 0 => println(dmg) // TODO: do some diminishing returnish something
            }
        }

    }
  }

}