package game.entity.ui

import core.render.Textured
import core.engine.entity.DynamicEntityFactory
import core.engine.entity.AlphaIndex

class IniciativePortrait(bx: Int, by: Int, w: Int, h: Int, portraitIdX: Int, portraitIdY: Int) extends Rectangle(bx, by, w, h) with Textured with DynamicEntityFactory[IniciativePortrait] with AlphaIndex {
  val alphaIndex = 20
  
  def create = {
    val n = new IniciativePortrait(bx, by, w, h, portraitIdX, portraitIdY)
    n.guid = this.guid
    n.renderData = this.renderData
    n
  }
  val textureName = "textures/portraits.png"
  bottomLeftUvs = (portraitIdX * 0.25f, portraitIdY * 0.25f)
  topRightUvs = (portraitIdX * 0.25f + 0.25f, portraitIdY * 0.25f + 0.25f)
}