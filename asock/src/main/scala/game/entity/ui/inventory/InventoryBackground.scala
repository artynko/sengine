package game.entity.ui.inventory

import core.render.Textured
import game.entity.ui.Rectangle
import core.render.AlphaTexture
import core.engine.entity.AlphaIndex

class InventoryBackground(bx: Int, by: Int, w: Int, h: Int) extends Rectangle(bx, by, w, h) with Textured with AlphaTexture with AlphaIndex {
  println("inventory background created")
  val alphaIndex = 18
  val textureName = "textures/inventory_bg.png"
  override val alphaTextureName = "textures/60alpha.bmp"
}