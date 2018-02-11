package game.entity.ui

import core.engine.entity.AlphaIndex
import core.engine.entity.Clickable

class Button(bx: Int, by: Int, w: Int, h: Int, labelText: String) extends Rectangle(bx, by, w, h) with AlphaIndex with Clickable {
  val alphaIndex = 30
  val label = new Label
  label.text = labelText
  label.move(bx + x + 3, ((by + y) - h) + 3, 0)

  override def onMove() = {
    label.move(bx + x + 3,  (by + y) + 6, 0)
  }

}