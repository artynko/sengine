package game.map.generator

import game.app.CityTile
import game.app.Industrial
import game.app.Farm
import game.app.Empty

class GeneratorRepository {
  
  val cityGenerator = new CityGenerator
  val singleTileGenerator = new SingleTileGenerator
  val farmGenerator = new FarmGenerator
  
  def findGenerator(tileType: GlobalTileType): TileGenerator = tileType match {
    case CityTile(_) => cityGenerator
    case Empty() => singleTileGenerator
    case Farm(_) => farmGenerator
    case Industrial() => singleTileGenerator
    case _ => throw new RuntimeException("No generator for tile type " + tileType)
  }

}