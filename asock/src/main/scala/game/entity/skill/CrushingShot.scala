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
 * Deals 2x dmg
 */
class CrushingShot extends TargetedSkill {
  val combatService = inject[CombatService]
  val log = injectActor[LogWindow]
  def cooldown = 4
  def iconPosition = (1, 0)

  def activate(ch: Character) = ch.current.rangedDmg.value = ch.current.rangedDmg.value * 2
  def deactivate(ch: Character) = ch.current.rangedDmg.value = ch.current.rangedDmg.value / 2
  def use(ch: Character, e: MeleeAiCharacter) = {
    log.add("Crushing Shot activated")
    combatService.rangedHit(ch, e)
  }

  def nextFrame(elapsedMs: Long) = {
  }

  def myTurnStart() = {}

}