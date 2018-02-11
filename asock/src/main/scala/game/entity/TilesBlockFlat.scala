package game.entity

import core.engine.entity.CompositeEntity
import core.engine.entity.StaticEntity
import core.render.FlatShader
import core.render.Guid
import core.render.Textured
import core.render.NonCulled
import core.render.AlphaTexture
import core.engine.entity.AlphaIndex

class TilesBlockFlat(ai: Int) extends CompositeEntity with StaticEntity with Guid with FlatShader with Textured with NonCulled with AlphaTexture with AlphaIndex {
  val alphaIndex = ai
  val textureName = "textures/grass.png"
  override val alphaTextureName = "textures/grass_alpha.bmp"

}