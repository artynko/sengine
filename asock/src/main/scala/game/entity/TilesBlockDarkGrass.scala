package game.entity

import core.engine.entity.CompositeEntity
import core.engine.entity.StaticEntity
import core.render.FlatShader
import core.render.Guid
import core.render.Textured
import core.render.NonCulled
import core.render.AlphaTexture
import core.engine.entity.AlphaIndex

class TilesBlockDarkGrass(ai: Int) extends CompositeEntity with StaticEntity with FlatShader with Guid with Textured with NonCulled with AlphaTexture with AlphaIndex {
  val alphaIndex = ai
  val textureName = "textures/grass_dark.png"
  override val alphaTextureName = "textures/grass_alpha.bmp"

}