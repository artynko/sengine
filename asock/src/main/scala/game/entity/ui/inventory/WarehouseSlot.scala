package game.entity.ui.inventory

import akka.actor.ActorContext
import game.entity.InventoryItem

class WarehouseSlot extends ItemSlot {
  var item: InventoryItem = _
  def nextFrame(elapsedMs: Long) = {
  }

  def handleMessage = {
    case msg =>
  }
}