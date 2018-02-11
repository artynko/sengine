package game.entity

import core.engine.entity.Entity
import core.render.Mesh
import scala.util.Random
import game.random.SeededRandom
import core.engine.entity.Rendered
import core.engine.entity.StaticEntity
import core.render.Textured
import core.engine.entity.AlphaIndex
import core.render.NonCulled
import core.render.AlphaTexture

class Grass extends Entity with Mesh with Rendered with AlphaIndex with Textured with NonCulled with AlphaTexture {
  val alphaIndex = 11
  val meshName = "grass"
  val textureName = "textures/grass.png"
  override val alphaTextureName = "textures/grass_alpha.bmp"
  rotateY(SeededRandom.r.nextInt().toFloat)
  val a = 1000
  val d = 10000
  move(SeededRandom.r.nextInt(a).toFloat / d, SeededRandom.r.nextInt(a).toFloat / d, SeededRandom.r.nextInt(a).toFloat / d)

}