package core.app

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.promise
import scala.math.abs
import scala.math.atan2
import scala.math.sqrt
import scala.math.tan
import scala.math.toRadians
import scala.util.Failure
import scala.util.Success

import com.hackoeur.jglm.Mat3
import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import com.hackoeur.jglm.Vec4
import com.jogamp.common.nio.Buffers

import core.jgml.utils.SVec3
import core.jgml.utils.SVec4
import core.opengl.FlatShaderProgram
import game.entity.SelectionEfect

object X extends App {
  new ApplicationContext;
  val ij = new InjectTest
  /*
  val m = new Mat4(2);
  val n = m.translate(SVec3(1, 1, 1))
  val ar = n.getBuffer().array()
  val zipped = 0 to ar.length zip ar flatMap ({
    case (i, v) if i > 10 => List()
    case (i, v) if i % 4 < 3 => List(v)
    case _ => List()
  })

  for (f <- ar)
    print(f + " ")
  println
  println(n)
  println(zipped)
  val m3 = new Mat3(zipped.toArray)
  println(m3)
  */

  val p = promise[Unit];
  val f = p.future
  println("prefuture");
  f onComplete {
    case Success(a) => println(a)
    case Failure(a) => println("noot")
  }
  p success
    println("pre ready")
  Await.ready(f, 1 seconds)
  println("post future")

  val l = for (n <- 0 until 9) yield n
  println(l)
  println(l.sliding(3, 3) foreach println)

  val v = new Vec4(2, 0, 0, 1)
  val m = new Mat4(1.0f).translate(SVec3(3, 2, -5))
  println(m)

  println(v.multiply(m))

  println(Buffers.SIZEOF_FLOAT)

  println(0 until 5 map { _ => 1 })

  val r = Matrices.rotate(toRadians(32).toFloat, SVec3(0, 1, 0)).transpose().transpose()
  val r31 = r.getColumn[Vec4](0).getZ().toDouble
  val r32 = r.getColumn[Vec4](1).getZ().toDouble
  val r33 = r.getColumn[Vec4](2).getZ().toDouble
  val r21 = r.getColumn[Vec4](0).getY().toDouble
  val r11 = r.getColumn[Vec4](0).getX().toDouble
  println(r)
  println(atan2(-r31, sqrt((r32 * r32 + r33 * r33))).toFloat)
  println(atan2(r32, r33).toFloat)
  println(atan2(r21, r11).toFloat)
  val d = Matrices.rotate(atan2(-r31, sqrt((r32 * r32 + r33 * r33))).toFloat, SVec3(0, 1, 0)).multiply(
    Matrices.rotate(atan2(r32, r33).toFloat, SVec3(1, 0, 0))).multiply(Matrices.rotate(atan2(r21, r11).toFloat, SVec3(0, 1, 0)))
  println(d)

  val x = collection.mutable.ListBuffer[collection.mutable.ListBuffer[String]]()

  val grid = 0 until 10 map (_ => collection.mutable.ListBuffer() ++= 0 until 10 map (_ => "o"))

  // x,y -> g,h,(px, py)

  val blockedClosure = (x: Int, y: Int) => (x, y) match {
    case (x, y) if x == 3 && y == 9 => false
    case (x, y) if x == 0 && y == 2 => true
    case (x, y) if x == 3 => true
    case _ => false
  }

  val route = pathfind((0, 2), (8, 0), blockedClosure)

  for (i <- 1 until route.size - 1)
    grid(route(i)._1)(route(i)._2) = "."

  grid(0)(2) = "s"
  grid(8)(0) = "e"
  grid foreach (x => println(x.mkString(" ")))
  
  println(0 until 1)
  
  
  println(tan(toRadians(45.0f / 2)) * 20)
  println(tan(toRadians(45.0f / 2)) * 12)
  println(tan(toRadians(45.0f / 2)) * 10)
  println(tan(toRadians(45.0f / 2)) * 5)

  def pathfind(start: (Int, Int), destination: (Int, Int), isBLocked: (Int, Int) => Boolean): List[(Int, Int)] = {
    val closedList = collection.mutable.Map[(Int, Int), (Int, Int)]()
    val openList = collection.mutable.Map[(Int, Int), (Int, Int, (Int, Int))]()
    val newBlockedClosure = (x: Int, y: Int) => (x, y) match {
      case (x, y) if (x == start._1 && y == start._2) || (x == destination._1 && y == destination._2) => false
      case (x: Int, y: Int) => isBLocked(x, y)
    }
    openList(start) = (0, 0, start) // the root square added to open list
    pathfind(start, start, destination, openList, closedList, isBLocked)
  }

  //F = G + H 
  //G = the movement cost to move from the starting point A to a given square on the grid, following the path generated to get there. 
  //H = the estimated movement cost to move from that given square on the grid to the final destination, point B. This is often referred to as the heuristic, which can be a bit confusing. The reason why it is called that is because it is a guess. We really don’t know the actual distance until we find the path, because all sorts of things can be in the way (walls, water, etc.). You are given one way to calculate H in this tutorial, but there are many others that you can find in other articles on the web.
  def pathfind(current: (Int, Int), start: (Int, Int), destination: (Int, Int), openList: collection.mutable.Map[(Int, Int), (Int, Int, (Int, Int))], closedList: collection.mutable.Map[(Int, Int), (Int, Int)], isBLocked: (Int, Int) => Boolean): List[(Int, Int)] = {
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
      val h = abs(ntx - destination._1) + abs(nty - destination._2)
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
    pathfind(nextTile._2, start, destination, openList, closedList, isBLocked)
  }
  
  val aa = new SelectionEfect
  aa match {
    case FlatShaderProgram(e) => println("neeee")
    case _ => println("naaa")
  }
  
  
  val rotation = Matrices.rotate(toRadians(0).toFloat, SVec3(0, 1, 0))
  val newMat = new Mat4(1.0f).translate(SVec3(1, 0, 1)).multiply(rotation).translate(SVec3(0, 0, -1))
  println(newMat)
  println(SVec4(0, 0, 1, 1).multiply(newMat))
  
  
  val scaleM = new Mat3(SVec3(2, 0, 0), SVec3(0, 1, 0), SVec3(0, 0, 2))
  
  println(scaleM)
  val scale = 2.0f
  val scaleMatrix = new Mat4(SVec4(scale, 0, 0, 0), SVec4(0, scale, 0, 0), SVec4(0, 0, scale, 0), SVec4(0, 0, 0, 0))
  println(scaleMatrix)
  val asd = new Mat4(new Mat3(scale))
  println(asd)
  
}

class InjectTest {
  val d = inject[String]

  def inject[T](implicit m: Manifest[T]): T = {
    return ApplicationContext.get.getInstance(m.erasure).asInstanceOf[T]
  }

  def i(body: => Integer) = {
    body
  }

}