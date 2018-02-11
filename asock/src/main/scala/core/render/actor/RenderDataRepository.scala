package core.render.actor

import core.engine.messages.GetImplementation
import akka.actor.Actor
import core.render.Guid
import core.engine.entity.Entity
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

case class EntityRenderData(entity: Entity with Guid, vertexData: Array[Float], indexData: Array[Int])

/**
 * The idea behind this is that it should provide synchronized access to data that should be loaded dynamically
 * it is important that all the functions in this take only fraction of time as the retrieval of the next entity to
 * be loaded happens synchronously in the main rendering thread
 */
class RenderDataRepository extends Actor {
  implicit val timeout = Timeout(1000 second)

  var dataToLoad = List[EntityRenderData]()

  def receive = {
    case ('addData, entity: Entity with Guid, vertexData: Array[Float], indexData: Array[Int]) => dataToLoad = EntityRenderData(entity, vertexData, indexData) :: dataToLoad
    case 'retrieveFirst => dataToLoad match {
      case head :: tail =>
        dataToLoad = tail
        sender ! head
      case Nil => // empty
    }
    case GetImplementation => sender ! this
  }

  def add(entity: Entity with Guid, vertexData: Array[Float], indexData: Array[Int]) = self ! ('addData, entity, vertexData, indexData)
  def retrieveFirst() = Await.result(self ? 'retrieveFirst mapTo manifest[EntityRenderData], 5 seconds)
  /**
   * Returns true if there is something ready to be roaded
   */
  def ready() = dataToLoad.size > 0

}