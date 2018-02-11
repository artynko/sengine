package core.engine.entity.primitive

import com.hackoeur.jglm.Vec4
import core.render.Guid
import core.engine.entity.Entity

trait PrimitiveBox2D extends Guid with Primitive2D { this: Entity =>
  var bottomLeft: (Int, Int) = (0,  0)  
  var topRight: (Int, Int) = (10,  10)  
  var color = new Vec4(0, 0, 0, 1)
  var zIndex = 0.0f
  var bottomLeftUvs: (Float, Float) = (0, 0)
  var topRightUvs: (Float, Float) = (1, 1)
  
  def height = topRight._2 - bottomLeft._2 
  def width = topRight._1 - bottomLeft._1 
  def left = x + bottomLeft._1
  def top = y + bottomLeft._2 + height
  def inBounds(x: Int, y: Int) = (left <= x && left + width >= x) && (top >= y && top - height <= y)
  
}