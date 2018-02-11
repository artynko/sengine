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
import core.engine.entity.DynamicEntity
import core.render.Textured
import core.render.AlphaTexture
import game.map.generator.Tile
import core.engine.entity.AlphaIndex
import core.render.NoDepthTest
import core.render.NonCulled
import core.render.FlatShader

class TargetingSphere extends StaticEntity with Mesh with Rendered with Textured with NoDepthTest with AlphaTexture with AlphaIndex with FlatShader with NonCulled {
  val alphaIndex = 20 
  val meshName = "targeting_sphere"
  val textureName = "textures/targetTile.png"
  override val alphaTextureName = "textures/20alpha.bmp"
}