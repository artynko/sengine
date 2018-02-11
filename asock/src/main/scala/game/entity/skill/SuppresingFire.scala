package game.entity.skill

import core.engine.entity.Entity
import game.entity.HasStats
import game.entity.character.Character
import game.entity.computer.character.MeleeAiCharacter
import game.entity.ui.LogWindow
import game.entity.ui.SkillIcon
import game.service.CombatService

/**
 * Sets the targets movement to 0 for this turn, the weapon does 2 less damage
 */
class SuppresingFire extends TargetedSkill {
  val combatService = inject[CombatService]
  val log = injectActor[LogWindow]
  def cooldown = 2
  def iconPosition = (2, 0)
  var enemy: Option[MeleeAiCharacter] = None
  var movement: Int = _
  val DURATION = 2
  var currentDuration: Int = _

  def activate(ch: Character) = ch.current.rangedDmg.value = ch.current.rangedDmg.value - 2
  def deactivate(ch: Character) = ch.current.rangedDmg.value = ch.current.rangedDmg.value + 2
  def use(ch: Character, e: MeleeAiCharacter) = {
    log.add("Supresing Fire activated")
    combatService.rangedHit(ch, e)
    if (e.base.movement.value > 0 && combatService.computeHit(ch, e) > 0) { // wasn't imobilized yet and is in range
      log.add("Enemy can't move this turn")
      enemy = Some(e)
      movement = e.base.movement.value
      e.base.movement.value = 0
      currentDuration = DURATION
    }
  }

  def nextFrame(elapsedMs: Long) = {
  }

  override def newTurn = {
    super.newTurn
    for (e <- enemy) { // if someone was affected by my skill, revert back his values
      currentDuration = currentDuration - 1
      if (currentDuration == 0) {
        e.base.movement.value = movement
        enemy = None
      }
    }
  }

  def myTurnStart() = {}

}