package game.entity.skill

import game.entity.computer.character.MeleeAiCharacter
import game.user.input.Selection
import java.awt.event.InputEvent
import game.entity.character.Character
import game.entity.ui.SkillIcon
import core.jgml.utils.SVec4
import core.engine.messages.Clicked

/**
 * A targeted skill with an icon
 */
abstract class TargetedSkill extends Skill {
  def iconPosition: (Int, Int)
  def cooldown: Int
  val _icon = new SkillIcon(0, 0, Skill.ICON_SIZE, Skill.ICON_SIZE, iconPosition._1, iconPosition._2)
  _icon.hide
  def icon = _icon
  var targetingActive = false

  def handleMessage = {
    case Clicked(Some(i), button, _) if icon == i && (button & InputEvent.BUTTON1_MASK) != 0 && !targetingActive && icon._cooldown == 0 =>
      targetingActive = true // my activation icon was clicked, and there is no cooldown
      icon.tintColorEnable(SVec4(0.8f, 0.8f, 0, 1))
      activate(Selection.selected.get)
    case Clicked(Some(i), button, _) if icon == i && (button & InputEvent.BUTTON1_MASK) != 0 && targetingActive =>
      targetingActive = false // clear the activation
      icon.tintColorDisable
      deactivate(Selection.selected.get)
    case Clicked(Some(e: MeleeAiCharacter), button, _) if (button & InputEvent.BUTTON1_MASK) != 0 && targetingActive =>
      val selected = Selection.selected.get
      use(selected, e) // use the skill
      deactivate(selected) // deactivate all the modifiers
      icon.cooldown(cooldown)
      icon.tintColorDisable
      targetingActive = false
      eventBus.send(SkillUsed(this, selected))
    case Clicked(Some(i), button, _) if (button & InputEvent.BUTTON1_MASK) != 0 && targetingActive => // I was targeting and someone clicked something else that wasn't me or target
      targetingActive = false
      icon.tintColorDisable
      deactivate(Selection.selected.get)
    case _ =>
  }

  def myTurnEnd() = {
    val selected = Selection.selected.get
    if (targetingActive) {
      deactivate(selected)
      icon.tintColorDisable
    }
    targetingActive = false
  }
  def newTurn() = icon.newTurn

  def activate(ch: Character)
  def deactivate(ch: Character)
  def use(ch: Character, e: MeleeAiCharacter)

}