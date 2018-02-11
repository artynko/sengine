package core.render

trait RenderLifecycleListener {
  // executed when a frame is rendered
  def frame() = {
    val m = collection.mutable.Map("x" -> 1);
  }
  
}