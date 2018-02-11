package game.entity

import core.engine.entity.Clickable
import core.engine.entity.Entity
import core.render.Mesh
import core.engine.messages.Clicked
import game.user.input.Selection
import core.engine.messages.Move
import core.engine.entity.EntityFactory
import core.engine.CameraService
import core.engine.entity.Rendered
import core.engine.entity.StaticEntity
import core.render.FlatShader
import scala.math.toRadians
import game.random.SeededRandom

class GrassTileModel extends Entity with Mesh with Rendered {
  val meshName = "tile"
  show
  rotateY(toRadians(SeededRandom.r.nextInt(3) * 90).toFloat)
}