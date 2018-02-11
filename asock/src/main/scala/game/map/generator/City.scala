package game.map.generator

object City {
  def apply(center: ModelTile) = new City(collection.mutable.ListBuffer(), center)
}

class City(val farms: collection.mutable.ListBuffer[ModelTile], val center: ModelTile) {


}