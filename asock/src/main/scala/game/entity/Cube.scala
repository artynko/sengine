package game.entity

import core.engine.entity.Entity
import core.render.Mesh
import core.engine.entity.Clickable
import core.engine.entity.DynamicEntity

class Cube extends DynamicEntity with Mesh {
  val meshName = "cube_w_n3"
  val rotationSpeed = 0.01f / 16.0f
    
  def nextFrame(elapsedMs: Long) = {
    //rotation += rotationSpeed * elapsedMs
  }
  def handleMessage = {
    case msg => println("message " + msg)
  }
}