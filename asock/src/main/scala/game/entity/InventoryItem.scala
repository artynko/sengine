package game.entity

import core.engine.entity.Rendered
import core.engine.entity.Entity
import core.engine.entity.StaticEntity
import core.engine.entity.AlphaIndex
import core.engine.entity.DynamicEntityFactory
import core.render.Border
import game.entity.skill.Skill

/**
 * A trait that tells that this item is a part of inventory, it doesn't mean it will be rendered only in inventory
 */
trait InventoryItem extends StaticEntity with Rendered with AlphaIndex with DynamicEntityFactory[Entity] with Border {
  /**
   * A list of skills this item grants
   */
  def skills: List[Skill] 
  val alphaIndex = 15
  borderSize = 4
  val inventoryScale: Float
  val inventoryXOffset: Float
  val inventoryYOffset: Float
}