package core.engine

import core.app.Component
import scala.math._

class PathFindingService extends Component {

  def pathfindA(start: (Int, Int), destination: (Int, Int), isBLocked: (Int, Int) => Boolean): List[(Int, Int)] = {
    val closedList = collection.mutable.Map[(Int, Int), (Int, Int)]()
    val openList = collection.mutable.Map[(Int, Int), (Int, Int, (Int, Int))]()
    val newBlockedClosure = (x: Int, y: Int) => (x, y) match {
      case (x, y) if (x == start._1 && y == start._2) || (x == destination._1 && y == destination._2) => false
      case (x: Int, y: Int) => isBLocked(x, y)
    }
    openList(start) = (0, 0, start) // the root square added to open list
    pathfindA(start, start, destination, openList, closedList, isBLocked)
  }

  //F = G + H 
  //G = the movement cost to move from the starting point A to a given square on the grid, following the path generated to get there. 
  //H = the estimated movement cost to move from that given square on the grid to the final destination, point B. This is often referred to as the heuristic, which can be a bit confusing. The reason why it is called that is because it is a guess. We really don’t know the actual distance until we find the path, because all sorts of things can be in the way (walls, water, etc.). You are given one way to calculate H in this tutorial, but there are many others that you can find in other articles on the web.
  def pathfindA(current: (Int, Int), start: (Int, Int), destination: (Int, Int), openList: collection.mutable.Map[(Int, Int), (Int, Int, (Int, Int))], closedList: collection.mutable.Map[(Int, Int), (Int, Int)], isBLocked: (Int, Int) => Boolean): List[(Int, Int)] = {
    val parent = openList(current)._3
    val parentG = openList(current)._1
    openList.remove(current)
    closedList(current) = parent
    if (current == destination) {
      // compute the route
      var route = List[(Int, Int)]()
      route = destination :: route
      while (route(0) != start) {
        route = closedList(route(0)) :: route // add parent for current head to route
      }
      return route
    }
    // update FGH for all surounding tiles
    for (ntx <- current._1 - 1 to current._1 + 1; nty <- current._2 - 1 to current._2 + 1 if ntx >= 0 && nty >= 0 && !isBLocked(ntx, nty) && !closedList.contains((ntx, nty))) {
      // update G
      val newG = if (current._1 == ntx || current._2 == nty) parentG + 10 else parentG + 14 // i.e I am in a line or diagonal
      val h = (abs(ntx - destination._1) + abs(nty - destination._2)) * 10
      openList.get((ntx, nty)) match {
        case None => openList((ntx, nty)) = (newG, h, current)
        case Some((g, h, (oldPx, oldpY))) if g > newG => openList((ntx, nty)) = (newG, h, current) // If my G is better then previous parent update
        case _ =>
      }
    }
    // find the best next open tile
    val nextTile = openList.foldLeft((10000, (0, 0))) {
      case ((bestF, (bx, by)), ((mx, my), (g, h, _))) if bestF > g + h => (g + h, (mx, my))
      case ((bestF, (bx, by)), _) => (bestF, (bx, by))
    }
    pathfindA(nextTile._2, start, destination, openList, closedList, isBLocked)
  }
}