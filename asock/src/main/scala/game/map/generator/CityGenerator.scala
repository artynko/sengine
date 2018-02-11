package game.map.generator

import game.app.CityTile
import scala.util.Random
import game.app.Unresolved

class CityGenerator extends TileGenerator {
  val r = new Random

  def handleNextTile(current: ModelTile, next: ModelTile) = {
    next.tileType match {
      case CityTile(_) => // next was already resolved to city so ignore it
      case _ if next.possibleTileTypes.size == 1 && next.possibleTileTypes(0).isInstanceOf[CityTile] => // I am already planned to be a city tile
      case _ =>
        next.tileType = Unresolved() // I want the next to be resolved even if something was already there, so it gets resolved again
        current.tileType match {
          case CityTile(0) =>
          case CityTile(1) =>
            if (r.nextInt(30) == 0)
              next.singlePossibleType(CityTile(1))
            if (r.nextInt(5) == 0)
              next.singlePossibleType(CityTile(0))
          case CityTile(2) =>
            if (r.nextInt(5) == 0)
              next.singlePossibleType(CityTile(1))
            else if (r.nextInt(15) == 0)
              next.singlePossibleType(CityTile(2))
          case CityTile(spread) =>
            if (r.nextInt(30) < 2)
              next.singlePossibleType(CityTile(spread))
            else if (r.nextInt(25) != 0)
              next.singlePossibleType(CityTile(spread - 1))
        }
    }
  }

}