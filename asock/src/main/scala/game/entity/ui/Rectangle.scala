package game.entity.ui

import core.engine.entity.primitive.PrimitiveBox2D
import core.engine.entity.StaticEntity
import core.engine.entity.UiElement2D
import core.render.Wireframe
import core.engine.entity.Rendered
import core.engine.entity.Entity
import core.render.FlatShader

class Rectangle(bx: Int, by: Int, w: Int, h: Int) extends Entity with UiElement2D with PrimitiveBox2D with Rendered with FlatShader {
  bottomLeft = (bx, by)
  topRight = (bx + w, by + h)

}