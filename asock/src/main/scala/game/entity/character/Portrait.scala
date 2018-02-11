package game.entity.character

import game.entity.ui.IniciativePanel
import core.app.Component
import core.engine.entity.Entity

trait Portrait { this: Entity =>
  val initiativePanel = injectActor[IniciativePanel]

  def assignPortrait(col: Int, row: Int) = initiativePanel.registerPortrait(this, col, row)
}