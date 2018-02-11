package game.entity.ui.inventory

import core.engine.entity.Clickable
import core.engine.entity.DynamicEntity
import core.engine.entity.Rendered
import core.engine.entity.UiElement2D
import core.engine.entity.primitive.PrimitiveBox2D
import core.render.AlphaTexture
import core.render.FlatShader
import core.render.Textured
import core.engine.entity.Entity
import game.entity.AssaultRifle
import game.entity.HeavyMg
import scala.math._
import game.entity.InventoryItem
import game.entity.InventoryItem
import game.entity.Pistol
import game.entity.Grenade
import core.engine.entity.AlphaIndex
import core.engine.messages.Clicked
import game.user.input.Inventory
import core.engine.messages.Released
import game.entity.Backpack
import game.entity.CombatTurtleneck
import game.entity.ArmorVest
import core.render.NoDepthTest
import game.entity.Shotgun
import game.entity.AssaultRifle

abstract class ItemSlot extends DynamicEntity with UiElement2D with PrimitiveBox2D with Rendered with FlatShader with Textured with Clickable with AlphaTexture with AlphaIndex {
  val alphaIndex = 20
  val textureName = "textures/inventory_grid.png"
  override val alphaTextureName = "textures/60alpha.bmp"
  var content: Option[InventoryItem] = None

  /**
   * Size in inventory tiles
   */
  def size(posx: Int, posy: Int, x: Int, y: Int) = {
    bottomLeft = (posx, posy)
    topRight = (bottomLeft._1 + 32 * x, bottomLeft._2 + 32 * y)
    topRightUvs = (0.125f * x, 0.125f * y)
  }

  def addContent(e: InventoryItem) = {
    println("add content" + e)
    val entity2d: InventoryItem = e match {
      case e: AssaultRifle => new AssaultRifle(false) with UiElement2D
      case e: Shotgun => new Shotgun(false) with UiElement2D
      case e: HeavyMg => new HeavyMg(false) with UiElement2D
      case e: Pistol => new Pistol with UiElement2D
      case e: Grenade => new Grenade(false) with UiElement2D
      case e: Backpack => new Backpack with UiElement2D
      case e: CombatTurtleneck =>
        val e = new CombatTurtleneck with UiElement2D
        e.z = 50f
        e
      case e: ArmorVest =>
        val e = new ArmorVest with UiElement2D
        e.z = 50f
        e
      case _ => e
    }
    entity2d.scale = e.inventoryScale
    entity2d.rotateY(toRadians(90).toFloat)
    entity2d.move(bottomLeft._1 + x + entity2d.inventoryXOffset, bottomLeft._2 + y + entity2d.inventoryYOffset, entity2d.z)
    if (!rendered) // if I am hidden hide the newly created 2d entity too
      entity2d.hide
    content = Some(entity2d)
  }

  override def onMove() = {
    for (c <- content)
      c.move(bottomLeft._1 + x + c.inventoryXOffset, bottomLeft._2 + y + c.inventoryYOffset, c.z)
  }

  override def hide() = {
    super.hide
    for (c <- content)
      c.hide
  }

  override def show() = {
    super.show
    for (c <- content)
      c.show
  }
  /**
   * Removes content of this inventory slot, destroying the entity that is removed
   */
  def removeContent() = content match {
    case None =>
    case Some(e) => e.destroy
  }

}