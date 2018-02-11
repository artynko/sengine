package core.render.actor

import akka.actor.Actor
import core.engine.messages.GetImplementation
import core.render.RenderingCore
import core.engine.entity.Entity
import core.render.Guid
import core.app.Component
import scala.concurrent._
import java.util.concurrent.Executors

/**
 * Prepares render data for the renderingCore in asynchronous fashion, once prepared there are sent to the repository which picks it up
 * it should not matter if the loading in here takes a long time as this class should never be accessed in synchronous fashion
 */
class AsyncRenderDataLoader extends Actor with Component {
  val executorService = Executors.newSingleThreadExecutor()
  implicit val executionContext = ExecutionContext.fromExecutorService(executorService)
  var renderingCore: RenderingCore = _ // injected when rendering core creates itself, as AsyncRenderDataLoader is part of rendering core
  val repository = injectActor[RenderDataRepository]
  var loadedNotProccessed = Set[Entity with Guid]()

  def receive = {
    case ('loadVertexData, e: Entity with Guid) => // I check if I haven't already processed this, since the messages come from renderingCore that is single threaded
      // loads the model sends the vertex data to repository
        println("future start")
        val data = renderingCore.getVertexIndexData(e, renderingCore.boundVaoMesh)
        repository.add(e, data.vertexData, data.indexes)
        println("future end")
    case GetImplementation => sender ! this
  }

  def loadVertexData(e: Entity with Guid) = self ! ('loadVertexData, e)

}