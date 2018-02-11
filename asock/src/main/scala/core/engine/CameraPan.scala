package core.engine

import core.engine.entity.DynamicEntity
import scala.math._
import core.app.RegisteringComponent
import core.engine.messages.MoveOrigin
import core.engine.messages.ZoomAndOffset

class CameraPan extends DynamicEntity with RegisteringComponent {
  var cameraService: CameraService = null // Camera pan is injected into camera service so it will then fill this property

  val speed = 0.05f / 16f
  var destination = (0f, 0f)
  var distanceX = 0f
  var distanceZ = 0f
  var speedX = 0f
  var speedZ = 0f
  val animate = collection.mutable.Map[Int, (Float, Float, Float, () => Float, Float => Unit)]()

  def nextFrame(elapsedMs: Long) = {
    if (animate.size > 0) {
      animate transform {
        case (key, (destination, speed, remainingDistance, getter, setter)) =>
          val newRemaining: Float = (remainingDistance - abs(speed * elapsedMs) < 0) match {
            case true =>
              setter(destination)
              0
            case false =>
              setter(getter() + speed * elapsedMs)
              remainingDistance - abs(speed * elapsedMs)
          }
          (destination, speed, newRemaining, getter, setter)
      }
    }
    val toBeRemoved = animate flatMap {
        case (key, (_, _, remainingDistance, _, _)) if remainingDistance <= 0 => Some(key)
        case _ => None
    }
    toBeRemoved foreach (animate.remove(_))
  }

  def handleMessage = {
    case MoveOrigin(ox, oz) =>
      // destination, speed, remainingdistance, function that returns the current value
      animate(0) = ((ox, (ox - cameraService.center._1) * speed, abs(dist(ox, cameraService.center._1)), () => cameraService.center._1, f => cameraService.center(f, cameraService.center._2)))
      animate(1) = ((oz, (oz - cameraService.center._2) * speed, abs(dist(oz, cameraService.center._2)), () => cameraService.center._2, f => cameraService.center(cameraService.center._1, f)))
    case ZoomAndOffset(xRotation: Float, xOffset: Float, distance: Float, height: Float) =>
      animate(2) = ((xRotation, (xRotation - cameraService.xRotation) * speed * 2, abs(dist(xRotation, cameraService.xRotation)), () => cameraService.xRotation, f => cameraService.xRotation = f))
      animate(3) = ((xOffset, (xOffset - cameraService.xOffset) * speed * 2, abs(dist(xOffset, cameraService.xOffset)), () => cameraService.xOffset, f => cameraService.xOffset = f))
      animate(4) = ((distance, (distance - cameraService.distance) * speed * 2, abs(dist(distance, cameraService.distance)), () => cameraService.distance, f => cameraService.distance = f))
      animate(5) = ((height, (height - cameraService.height) * speed * 2, abs(dist(height, cameraService.height)), () => cameraService.height, f => cameraService.height = f))
    case _ =>
  }

  def dist(a: Float, b: Float) = max(a, b) - min(a, b)

}