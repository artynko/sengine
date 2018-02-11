package game.entity

import core.engine.entity.CompositeEntity
import core.engine.entity.StaticEntity
import core.render.Guid
import core.render.FlatShader
import core.render.Textured
import core.render.NonCulled
import core.engine.entity.AlphaIndex

class TilesBlock extends CompositeEntity with StaticEntity with Guid with Textured with AlphaIndex {
  val alphaIndex = 13
  val textureName = "textures/tree.png"

}