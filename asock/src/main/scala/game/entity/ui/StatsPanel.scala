package game.entity.ui

import core.engine.entity.DynamicEntity
import akka.actor.ActorContext
import core.engine.entity.EntityFactory
import core.jgml.utils.SVec4
import game.user.input.Selection
import game.entity.HasStats
import game.core.Lifecycle

class StatsPanel extends DynamicEntity {
  val hudText = EntityFactory.createStatic[Label]
  hudText.color = SVec4(1, 1, 1, 1) // white
  hudText.size = 22
  hudText.x = 50 
  hudText.y = 250
  def nextFrame(elapsedMs: Long) = {
    Selection.selected match {
      case Some(character: HasStats) => 
        hudText.text = "HP: " + character.current.hp.value + "/" + character.max.hp.value + "\n" +
                       "I: " + character.current.iniciative.value + "/" + character.max.iniciative.value + "\n" +
                       "MOV: " + character.current.movement.value + "/" + character.max.movement.value + "\n" +
                       "M DMG: " + character.current.meeleDmg.value + "/" + character.max.meeleDmg.value + "\n" +
                       "M DEF: " + character.current.meeleDefense.value + "/" + character.max.meeleDefense.value + "\n" +
                       "R DMG: " + character.current.rangedDmg.value + "/" + character.max.rangedDmg.value + "\n" + 
                       "RNG: " + character.current.range.value + "/" + character.max.range.value + "\n" 
      case _ =>
    }
  }
  def handleMessage = {
    case msg =>
  }
}