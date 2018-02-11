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

class TileTarget extends StaticEntity with Mesh with Rendered with Clickable with Textured with NoDepthTest with AlphaTexture with AlphaIndex {
  val alphaIndex = 13 
  var target: Tile = null
  var remainingMovement: Double = 0
  val meshName = "tileTarget"
  val textureName = "textures/targetTile.png"
  override val alphaTextureName = "textures/targetTile_alpha.bmp"
  override def y_=(yy: Float) = super.y = yy + 0.0001f
}