package core.engine.messages

import core.engine.entity.Entity
import scala.concurrent.Promise
import com.hackoeur.jglm.Vec3

// used by Entities for processing
case class ProcessFrame(elapsedMs: Long) 
case class FrameProcessed
case class Clicked(entity: Option[Entity], modifier: Int, rayDirection: Vec3) // entity, button (e.getModifiers)
case class Pressed(guidModifier: (Entity, Int)) // entity, button (e.getModifiers)
case class Released(guidModifier: (Entity, Int)) // entity, button (e.getModifiers)
case class Moved(guidModifier: (Entity, Int, Int, Int)) // entity, button (e.getModifiers), x, y
case class ClickedRayCasted(rayDirection: Vec3) // send every time a button is clicked, the direction of the ray casted from camera
case class KeyPressed(key: List[String])
case class Name(name: String)
case class GetImplementation
case class RenderingDone

// entities for transformations
case class Move(x: Float, y: Float, z: Float)

// used by EngineCoreActor
case class StartFrameProcessing(p: Promise[Unit])
case class RegisterEntity(entity: Entity)
case class UnregisterEntity(entity: Entity)
case class GetEntities

// camera pan effects
case class MoveOrigin(x: Float, z: Float)
case class ZoomAndOffset(xRotation: Float, xOffset: Float, distance: Float, height: Float)