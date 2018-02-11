package core.render

import core.jgml.utils.SVec4
import com.hackoeur.jglm.Vec4

trait Tinted {
  
  var tintColor: Vec4 = SVec4(0, 0, 0, 0)
  def tintColorEnable(color: Vec4) = tintColor = color
  def tintColorDisable() = tintColor = SVec4(0, 0, 0, 0)

}