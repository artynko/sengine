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

/**
 * A skill that allows the weapon to fire 2 times, but gives it a hit chance penalty
 */
class RapidFire extends TargetedSkill {
  def cooldown = 3
  def iconPosition = (0, 0)
  val combatService = inject[CombatService]
  val log = injectActor[LogWindow]
  val HIT_PENALTY = 30

  def activate(ch: Character) = ch.current.hitPenalty.value += HIT_PENALTY
  def deactivate(ch: Character) = ch.current.hitPenalty.value -= HIT_PENALTY
  def use(ch: Character, e: MeleeAiCharacter) = {
    log.add("Rapid Fire activated")
    combatService.rangedHit(ch, e)
    combatService.rangedHit(ch, e)
  }

  def nextFrame(elapsedMs: Long) = {
  }

  def myTurnStart() = {}

}