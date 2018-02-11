package core.render

import core.jgml.utils.SVec3

/**
 * Shows border around an object
 */
trait Border {
  private var _borderVisible = false
  private var _borderColor = SVec3(1, 1, 1)
  private var _borderSize = 1
  def borderShow() = _borderVisible = true
  def borderHide() = _borderVisible = false
  def borderVisible = _borderVisible
  def borderColor(r: Float, g: Float, b: Float) = _borderColor = SVec3(r, g, b)
  def borderColor() = _borderColor
  def borderSize = _borderSize
  def borderSize_=(size: Int) = _borderSize = size
}