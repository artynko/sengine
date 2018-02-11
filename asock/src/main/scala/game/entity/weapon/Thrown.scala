package game.entity.weapon

import core.render.Textured
import game.entity.HasStats
import game.entity.InventoryItem
import core.render.Mesh

trait Thrown extends InventoryItem with Mesh with Textured with HasStats {

}