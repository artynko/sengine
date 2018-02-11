package game.map.entity

import core.engine.entity.DynamicEntity
import core.engine.entity.Rendered
import core.render.Mesh
import core.render.Textured
import game.entity.messages.SquadMoved
import core.engine.entity.Clickable
import core.engine.entity.StaticEntity

class City3Model extends StaticEntity with Mesh with Rendered with Textured {
  val meshName = "global_map/city3"
  val textureName = "textures/globalTileSet.png"


}