package core.engine.entity

trait Rendered {
  private var _rendered = true
  def rendered() = _rendered
  def hide() = _rendered = false
  def show() = _rendered = true 
  def toogleRendered() = _rendered = !_rendered
}