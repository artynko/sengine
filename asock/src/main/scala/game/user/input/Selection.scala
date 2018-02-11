package game.user.input

import core.engine.entity.Entity
import core.engine.entity.EntityFactory
import game.entity.SelectionEfect
import game.entity.Bar

object Selection {
  def moveEffect(x: Float, y: Float, z: Float) = if (effect != null) effect.move(x, y, z)
  def showEffect() = if (effect != null) effect.show
  var selected: Option[game.entity.character.Character] = None
  var playerTarget: Option[Entity] = None
  var effect: SelectionEfect = null
  var bar: Bar = null
}