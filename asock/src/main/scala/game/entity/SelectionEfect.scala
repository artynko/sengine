package game.entity

import core.engine.entity.Entity
import core.render.Mesh
import core.engine.entity.Rendered
import core.render.FlatShader
import core.engine.entity.StaticEntity
import core.engine.entity.DynamicEntity
import core.engine.entity.AlphaIndex
import core.render.Textured
import core.render.NoDepthTest

class SelectionEfect extends StaticEntity with Mesh with Rendered with FlatShader with NoDepthTest with AlphaIndex with Textured {
  val alphaIndex = 13
  val textureName = "textures/gun_effect.png"
  override val alphaTextureName = "textures/targetTile_alpha.bmp"
  val meshName = "selection"
  override def y_=(yy: Float) = super.y = yy + 0.0002f

}