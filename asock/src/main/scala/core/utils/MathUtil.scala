package core.utils

import core.engine.entity.Entity
import scala.math._
import core.engine.Point
import com.hackoeur.jglm.Vec3

object MathUtil {

  def dist(a: Float, b: Float) = max(a, b) - min(a, b)

  def distance(e1: Entity, e2: Entity) = {
    val xdist = abs(e1.x - e2.x)
    val ydist = abs(e1.z - e2.z)
    sqrt(pow(xdist, 2) + pow(ydist, 2)).toInt
  }
  def distance(p1: Point, p2: Point) = {
    val xdist = abs(p1.x - p2.x)
    val ydist = abs(p1.y - p2.y)
    sqrt(pow(xdist, 2) + pow(ydist, 2)).toInt
  }
  def distance(p1: Vec3, p2: Vec3) = {
    val xdist = abs(p1.getX - p2.getX)
    val ydist = abs(p1.getY - p2.getY)
    val zdist = abs(p1.getZ - p2.getZ)
    sqrt(pow(xdist, 2) + pow(ydist, 2) + pow(zdist, 2)).toFloat
  }

  def planeRayIntersection(planeNormal: Vec3, planeDist: Float, rayOrigin: Vec3, rayNormal: Vec3): Vec3 = {
    val distanceFromOrigin = -((rayOrigin.dot(planeNormal) + planeDist) / rayNormal.dot(planeNormal))
    println("distance from origin", distanceFromOrigin)
    rayOrigin.add(rayNormal.multiply(distanceFromOrigin))
  }
}