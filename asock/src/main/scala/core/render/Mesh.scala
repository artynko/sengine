package core.render

/**
 * A trait that specifies an object should be rendered as a mesh
 */
trait Mesh extends Guid {
  val meshName: String
}