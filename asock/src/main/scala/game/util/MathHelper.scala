package game.util
import scala.math._

object MathHelper {
  def dist(x: Float, y: Float, tx: Float, ty: Float) = {
    val xdist = abs(x - tx)
    val ydist = abs(y - ty)
    sqrt(xdist * xdist + ydist * ydist)
  }
}