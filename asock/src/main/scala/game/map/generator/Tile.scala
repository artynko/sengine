package game.map.generator

import core.engine.entity.Entity

object Tile {

  def apply(x: Int, y: Int, model: Entity) = {
    val r = new Tile
    r.x = x
    r.y = y
    r.model = model
    model.move(x * 2, -2.00001f, y * 2)
    r
  }

}

class Tile {
  var x = -1
  var y = -1
  var model: Entity = null
  var contains: Option[Entity] = None
}