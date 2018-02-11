package core.engine

import core.engine.entity.Entity
import akka.actor.Actor
import core.engine.messages.ProcessFrame
import akka.actor.Props
import core.engine.messages.StartFrameProcessing
import core.engine.messages.RegisterEntity
import scala.concurrent
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import akka.pattern.{ ask, pipe }
import core.engine.messages.GetEntities
import akka.util.Timeout
import akka.testkit.CallingThreadDispatcher
import core.engine.messages.GetImplementation
import core.engine.entity.DynamicEntity
import core.engine.entity.UiElement2D
import core.engine.messages.UnregisterEntity
import core.engine.messages.UnregisterEntity

class EngineCore {
  val actor = ActorFactory.create[EngineCoreActor]("engine-core-actor")
  implicit val timeout = Timeout(1000 second)
  val f = actor ? GetImplementation mapTo manifest[EngineCoreActor]
  val inst = Await.result(f, 5 seconds)

  def register(obj: Entity) = inst.self ! RegisterEntity(obj)
  def unregister(obj: Entity) =  Await.result(inst.self ? UnregisterEntity(obj), 5 seconds)

  def allEntities(): List[Entity] = entities ++ staticEntities ++ entities2d
  def entities(): List[Entity] = Await.result(inst.self ? 'dynamic mapTo manifest[List[Entity]], 30 seconds)
  def staticEntities(): List[Entity] = Await.result(inst.self ? 'static mapTo manifest[List[Entity]], 30 seconds)
  def entities2d(): List[Entity] = Await.result(inst.self ? 'entities2d mapTo manifest[List[Entity]], 30 seconds)
  
  /**
   * Sends only events doesn't simulate frame
   */
  def handleEventsOnly() = inst.sendEvents
  /**
   * Simulates frame and sends events
   */
  def handleFrame() {
    val p = promise[Unit]
    val f = p.future
    // I am sending a promise that I expect to be fullfilled once the entities are done with the processing
    actor ! StartFrameProcessing(p)
    Await.ready(f, 1000 seconds)
  }

}