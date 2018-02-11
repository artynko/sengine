package game.entity

import core.engine.entity.Entity
import core.render.Mesh
import core.engine.messages.FrameProcessed
import core.engine.entity.Clickable
import core.engine.entity.DynamicEntity
import core.render.Textured

class Sphere extends DynamicEntity with Mesh with Clickable with Textured {
  val textureName = "textures/male.png"
  val meshName = "monkey"
  val speed = 0.4f / 16.0f // my speed of movement is 1f per second
  val rotationSpeed = 0.03f / 16.0f
  var b = false;
  var rotation: Float = 0

  def nextFrame(elapsedMs: Long) = {
    /*
    rotation += rotationSpeed * elapsedMs
    rotateY(rotation)
    //rotation += 0.03f
    b match {
      case true => z += speed * elapsedMs
      case false => z -= speed * elapsedMs
    }
    if (z < -30) {
      b = true
      z = -30
    }
    if (z > -16) {
      b = false
      z = -16
    }
      
    for (i <- 1 until 1000)
      100 / i
      * 
      */
      
  }

  def handleMessage = {
    case msg => println("message " + msg)
  }
}