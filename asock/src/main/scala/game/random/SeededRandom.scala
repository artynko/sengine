package game.random

import scala.util.Random

object SeededRandom {
  val r = Random
  r.setSeed(123456)

}