package game.entity.ui.inventory

import akka.actor.ActorContext
import core.engine.messages.Moved
import core.render.Border
import core.engine.Display
import core.engine.ScalingService
import core.jgml.utils.SVec3
import core.jgml.utils.SVec4

class InventorySlot extends ItemSlot with Border {
  var slotId: Int = _
  var over = false

  def nextFrame(elapsedMs: Long) = {
  }

  def handleMessage = {
    case Moved((ee, m, x, y)) if (ee == this) =>
      over = true
      scale = 1.01f
      borderShow
    case Moved((ee, _, _, _)) if (ee != this) && over == true =>
      over = false
      scale = 1f
      borderHide
    case msg =>
  }
}