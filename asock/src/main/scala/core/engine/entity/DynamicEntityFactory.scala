package core.engine.entity

trait DynamicEntityFactory[T] {
  def create: T
}