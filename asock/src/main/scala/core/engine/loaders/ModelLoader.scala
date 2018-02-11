package core.engine.loaders

trait ModelLoader {
  /**
   * Loads model data from a source, returns the array of vertices, and an array of indexes that would produce the object
   * (note: this is coupled with opengl in very tight way)
   */
  def loadModel(): (Array[Float], Array[Int])
}