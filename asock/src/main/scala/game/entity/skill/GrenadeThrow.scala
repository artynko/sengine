package game.entity.skill

import game.entity.HasStats
import core.engine.entity.Entity
import game.entity.ui.SkillIcon
import core.engine.messages.Clicked
import game.entity.computer.character.MeleeAiCharacter
import java.awt.event.InputEvent
import game.entity.ui.LogWindow
import game.core.Lifecycle
import game.user.input.Selection
import game.service.CombatService
import game.entity.character.Character
import game.entity.TargetingSphere
import core.engine.messages.Moved
import game.repository.EnemyRepository
import scala.collection.mutable.ListBuffer
import core.utils.MathUtil
import core.jgml.utils.SVec4
import game.service.DealDamage
import game.entity.InventoryItem
import game.entity.messages.ItemUnequiped

/**
 * Does an aoe dmg in a radius
 */
class GrenadeThrow extends TargetedSkill {
  var item: InventoryItem = _ // the item that contains this skill
  val RADIUS = 4
  val DAMAGE = 4
  val RANGE = 25
  val combatService = inject[CombatService]
  val enemyRepository = inject[EnemyRepository]
  val log = injectActor[LogWindow]
  def cooldown = 1000
  def iconPosition = (3, 0)
  val inRange = ListBuffer[MeleeAiCharacter]()

  // state
  var oldRange: Int = _
  var oldHitPenalty: Int = _
  var oldDamage: Int = _

  // targeting sphere
  val ts = new TargetingSphere
  ts.move(40, -2, 20)
  ts.scale = RADIUS
  ts.hide

  override def handleMessage = {
    case ItemUnequiped(_, _, i) if i == item =>
      item.destroy
      ts.destroy
    case Moved((e: MeleeAiCharacter, _, x, y)) if targetingActive =>
      ts.show
      ts.move(e.x, e.y, e.z)
      // filter to all that are in range
      inRange foreach (_.tintColorDisable)
      inRange.clear
      inRange ++= enemyRepository().filter ( enemy => MathUtil.distance(e, enemy) <= RADIUS )
      inRange foreach (_.tintColorEnable(SVec4(0.7f, 0.2f, 0, 0.5f)))
    case Moved((_, _, _, _)) =>
      cleanup
    case m => super.handleMessage(m)
  }
  override def myTurnEnd() = {
    super.myTurnEnd
    cleanup
  }

  def activate(ch: Character) = {
    oldHitPenalty = ch.current.hitPenalty.value
    oldRange = ch.current.range.value
    oldDamage = ch.current.rangedDmg.value
    ch.current.hitPenalty.value = 0
    ch.current.range.value = RANGE
    ch.current.rangedDmg.value = DAMAGE
  }

  def deactivate(ch: Character) = {
    ch.current.hitPenalty.value = oldHitPenalty
    ch.current.range.value = oldRange
    ch.current.rangedDmg.value = oldDamage
  }

  def use(ch: Character, e: MeleeAiCharacter) = {
    if (combatService.computeHit(ch, e) > 0) {
      log.add("Grenade thrown")
      inRange foreach { ee =>
        ee.self ! DealDamage(DAMAGE)
        log.add(DAMAGE + " damage iflicted")
      }
    } else {
      log.add("Grenade throw missed")
    }
    ch.removeFromSlot(item)
  }

  def nextFrame(elapsedMs: Long) = {
  }

  def myTurnStart() = {}

  private def cleanup: Unit = {
    ts.hide
    inRange foreach (_.tintColorDisable)
    inRange.clear
  }

}