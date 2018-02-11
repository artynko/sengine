package game.map.entity

import core.engine.entity.DynamicEntity
import core.render.Textured
import core.render.Mesh
import core.engine.entity.EntityFactory
import core.engine.messages.ClickedRayCasted
import core.engine.CameraService
import core.utils.MathUtil
import core.jgml.utils.SVec3
import core.engine.messages.Clicked
import core.engine.EventBus
import game.entity.messages.SquadMoved

object SquadEntity {
  def apply() = EntityFactory.create[SquadEntity]
}

/**
 * An entity that represens a squad on the global map
 * @author arty
 *
 */
class SquadEntity extends DynamicEntity with Mesh with Textured {
  val cameraService = inject[CameraService]
  val eventBus = injectActor[EventBus]
  val meshName = "global_map/squadMarker"
  val textureName = "textures/test.png"
  val speed = 0.0003f
  var destination = SVec3(0, 0, 0)

  def nextFrame(elapsedMs: Long) = {
    val curSpeed = elapsedMs * speed
    val myPos = SVec3(x, y, z)
    MathUtil.distance(myPos, destination) match {
      case 0 =>
      case distance if distance <= curSpeed => 
        internalMove(destination.getX, destination.getY, destination.getZ)
        eventBus.send(SquadMoved(this, position))
      case _ =>
        val direction = destination.subtract(myPos).getUnitVector()
        val newPos = myPos.add(direction.multiply(curSpeed))
        internalMove(newPos.getX, newPos.getY, newPos.getZ)
        eventBus.send(SquadMoved(this, position))
    }

  }

  def handleMessage = {
    case Clicked(None, _, rayDirection) => destination = MathUtil.planeRayIntersection(SVec3(0, 1, 0), -0.00f, cameraService.cameraWorldSpace, rayDirection)
    case Clicked(Some(f: Farm1Model), _, _) => destination = f.position
    case _ =>
  }

}