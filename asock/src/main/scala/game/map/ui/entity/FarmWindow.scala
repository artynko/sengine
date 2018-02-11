package game.map.ui.entity

import core.render.Textured
import core.engine.entity.AlphaIndex
import core.render.NoDepthTest
import game.entity.ui.Rectangle

class FarmWindow extends Rectangle(0, 0, 590, 326) with Textured with AlphaIndex with NoDepthTest {
    override val alphaTextureName = "textures/95alpha.bmp"
    val alphaIndex = 30
    val textureName = "textures/farm_bg.png"
}