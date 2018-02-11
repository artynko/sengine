package game.entity

import core.engine.entity.Entity
import core.render.Mesh
import core.engine.entity.UiElement3D
import core.engine.entity.Rendered
import core.render.FlatShader
import core.engine.entity.DynamicEntity
import core.render.Textured
import core.engine.entity.AlphaIndex

class Bar extends DynamicEntity with Mesh with UiElement3D with FlatShader with Rendered with Textured with AlphaIndex {
  val alphaIndex = 15
  val meshName = "bar"
  val textureName = "textures/target_effect.png"
  override val alphaTextureName = "textures/target_effect_alpha.bmp"
  def nextFrame(elapsedMs: Long) = {
  }

  def handleMessage = {
    case msg => 
  }

}