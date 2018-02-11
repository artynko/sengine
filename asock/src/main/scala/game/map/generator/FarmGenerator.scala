package game.map.generator

import game.app.CityTile
import scala.util.Random
import game.app.Unresolved
import game.app.Empty
import game.app.Unresolved
import game.app.Farm

class FarmGenerator extends TileGenerator {
  val r = new Random

  def handleNextTile(current: ModelTile, next: ModelTile) = {
    next.tileType match {
      case Unresolved() => generateFarm(current, next) // only generate farm if there is space 
      case Empty() => generateFarm(current, next)
      case _ => // do nothing
    }
  }

  private def generateFarm(current: ModelTile, next: ModelTile) = {
    current.tileType match {
      case Farm(1) if r.nextInt(4) < 3 => 
        next.tileType = Unresolved()
        next.singlePossibleType(Farm(0))
      case _ => 
    }
  }

}