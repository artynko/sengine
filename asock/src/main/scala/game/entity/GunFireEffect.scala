package game.entity

import core.engine.entity.Entity
import core.render.Mesh
import core.engine.entity.Rendered
import core.render.FlatShader
import core.engine.entity.StaticEntity
import core.engine.entity.DynamicEntity
import core.render.Textured
import core.render.NonCulled
import core.engine.entity.AlphaIndex

class GunFireEffect extends StaticEntity with Mesh with Rendered with Textured with NonCulled with AlphaIndex {
  val alphaIndex = 20
  val meshName = "gun_effect"
  val textureName = "textures/gun_effect.png"
  override val alphaTextureName = "textures/fire_alpha.bmp"
  hide

}