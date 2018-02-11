package game.entity.ui

import core.engine.entity.DynamicEntity
import game.entity.InventoryItem
import game.user.input.Inventory
import core.engine.messages.Released
import core.engine.messages.Pressed
import game.entity.ui.inventory.ItemSlot
import game.entity.AssaultRifle
import game.entity.armor.Armor
import game.entity.ui.inventory.InventorySlot
import game.entity.ui.inventory.WarehouseSlot
import core.engine.ScalingService
import core.jgml.utils.SVec4
import core.engine.CameraService
import core.jgml.utils.SVec3

/**
 * Implements drag & drop of inventory items, it would be great to have this in the slots itself but then they may delete a dragged item for each other
 */
class InventoryDragAndDrop extends DynamicEntity {
  val inventory = inject[Inventory]
  val cameraService = inject[CameraService]
  var dragged: Option[InventoryItem] = None
  
  def nextFrame(elapsedMs: Long) = {
  }
  def handleMessage = {
    case Pressed((entity: WarehouseSlot, mod)) => dragged = Some(entity.item)
    case Released((entity: InventorySlot, mod)) if dragged.isDefined =>
      dragged match {
        case Some(n: Armor) => 
          //val newA = n.getClass().newInstance()
          inventory.equipArmor(n.create.asInstanceOf[Armor])
        case Some(n) => inventory.addToSlot(entity.slotId, dragged.get.create.asInstanceOf[InventoryItem]) // create a new instance to be added
        case None =>
      }
      dragged = None
    case Released(_) => dragged = None
    case msg =>

  }

}