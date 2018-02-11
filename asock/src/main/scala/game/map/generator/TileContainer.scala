package game.map.generator

import akka.pattern.ask
import akka.pattern.pipe
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import akka.util.Timeout
import core.app.RegisteringComponent

/**
 * Contains all tiles in current map
 */
case class AddTiles(tiles: List[List[Tile]])
case class OccupyTile(x: Int, y: Int, e: Entity)
case class IsOccupied(x: Int, y: Int)
case class VacantTile(x: Int, y: Int)

class TileContainer extends DynamicEntity with RegisteringComponent {
  // TODO: may be normal list instead of muttable
  val tiles = collection.mutable.ListBuffer[collection.mutable.ListBuffer[Tile]]()

  def nextFrame(elapsedMs: Long) = {
  }

  def handleMessage = {
    case 'clear => tiles.clear
    case AddTiles(t) => 
      tiles.clear
      tiles ++= t map ( collection.mutable.ListBuffer() ++= _ )
    case OccupyTile(xx, yy, e) => 
      tiles(xx)(yy).contains match {
        case Some(e) => sender ! Some(tiles(xx)(yy))
        case None => 
          tiles(xx)(yy).contains = Some(e) 
          sender ! None
      }
    case IsOccupied(xx, yy) if xx < 0 || yy < 0 => sender ! Some[Entity](null)
    case IsOccupied(xx, yy) => 
      val msg = tiles match {
        case t if t.size > xx && t(xx).size > yy => tiles(xx)(yy).contains 
        case _ => Some[Entity](null)
      }
      sender ! msg
    case VacantTile(xx, yy) => tiles(xx)(yy).contains = None
    case _ =>
  }
  
  def clear() = self ! 'clear
  
  def vacantTile(x: Int, y: Int) = self ! VacantTile(x, y)
  
  def addTiles(tiles: List[List[Tile]]) = self ! AddTiles(tiles)

  def occupyTile(x: Int, y: Int, e: Entity) = await[Option[Entity]](OccupyTile(x, y, e))

  def isOccupied(x: Int, y: Int) = await[Option[Entity]](IsOccupied(x, y))
  
  def at(x: Int, y: Int) = (x, y) match {
    case (x, y) if x >= 0 && y >= 0 => tiles(x)(y)
    case _ => null // TODO: fix this to not return null poitners
  }
  
  
  def modelBlock(startX: Int, startY: Int, size: Int) = {
    (for (x <- startX until startX + size; y <- startY until startY + size) yield tiles(x)(y).model) 
  }
}