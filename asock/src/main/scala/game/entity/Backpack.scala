package game.entity

import core.engine.entity.StaticEntity
import core.engine.entity.Rendered
import core.render.Mesh
import core.engine.entity.UiElement2D
import core.render.Textured
import game.entity.skill.Skill

class Backpack extends Mesh with Textured with InventoryItem {
  def skills = List[Skill]()
  def create = new Backpack
  val meshName = "backpack"
  val textureName = "textures/test.png"

  val inventoryScale = 120f
  val inventoryXOffset = 125f
  val inventoryYOffset = 40f
}