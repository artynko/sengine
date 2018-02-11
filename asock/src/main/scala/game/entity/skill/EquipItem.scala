package game.entity.skill

import core.engine.EngineCore
import game.entity.character.Character
import game.entity.computer.character.MeleeAiCharacter
import game.entity.messages.ItemUnequiped
import game.entity.ui.LogWindow
import game.entity.weapon.Weapon

abstract class EquipItem extends TargetedSkill {
  val log = injectActor[LogWindow]
  def cooldown = 0
  var weapon: Weapon = _
  var mySlot: Int = _
  var myCharacter: Character = _

  override def handleMessage = {
    case ItemUnequiped(character, slotId, item) if item == weapon && slotId != 0 =>
      mySlot = slotId // event thrown when the associated weapon was unequiped
      myCharacter = character
    case ItemUnequiped(character, slotId, item) if item != weapon && character == myCharacter =>
      // hopefully uneqip from whatever was in the slot I equiped this item, my character check is required 
      // since there can be many EquipItem objects used
      //println(s"adding to slot $mySlot, $item")
      character.addToSlot(mySlot, item) //  equip it to where this weapon was before
      myCharacter = null
    case m => super.handleMessage(m)
  }

  def activate(ch: Character): Unit = {}
  def deactivate(ch: Character): Unit = {}
  def use(ch: Character, e: MeleeAiCharacter): Unit = {
    log.add("Equiping " + weapon.getClass().getSimpleName())
    ch.removeFromSlot(weapon)
    ch.addToSlot(0, weapon)
  }

  def nextFrame(elapsedMs: Long) = {
  }

  def myTurnStart() = {}

}