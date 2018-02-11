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
import game.entity.messages.SquadMoved
import game.map.ui.entity.FarmWindow

object Farm1Model {
  def apply() = EntityFactory.create[Farm1Model]
  //def apply() = new Farm1Model
}

class Farm1Model extends DynamicEntity with Mesh with Textured with Clickable {
  val farmWindow = new FarmWindow()
  farmWindow.hide()
  farmWindow.move(100, 550, 0)
  val meshName = "global_map/farm1"
  val textureName = "textures/globalTileSet.png"
  def nextFrame(elapsed: Long) = {

  }

  def handleMessage = {
    case SquadMoved(_, pos) =>
      if (pos == position) {
        println("arived at " + this)
        farmWindow.show
      } else {
        farmWindow.hide
      }
    case _ =>
  }
}