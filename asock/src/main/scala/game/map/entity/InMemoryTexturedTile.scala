package game.map.entity

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
import core.render.texture.InMemoryTexture
import core.render.FlatShader

class InMemoryTexturedTile(size: Int) extends StaticEntity with Mesh with Rendered with InMemoryTexture with FlatShader with AlphaIndex {
  val alphaIndex = 1
  val meshName = "globalMapTile2"
  val textureWidth = size
  val textureHeight = size
  val textureData = new Array[Float](textureWidth * textureHeight * 3)
}