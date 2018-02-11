package core.engine.entity

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.Actor
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import core.engine.messages.FrameProcessed
import core.engine.messages.GetImplementation
import core.engine.messages.Move
import core.engine.messages.ProcessFrame
import core.engine.messages.RenderingDone
trait DynamicEntity extends Entity with Actor with Rendered {
  implicit val timeout = Timeout(1000 second)
  val eventQueue = collection.mutable.ListBuffer[Any]()

  def receive = {
    case RenderingDone => positionChanged = false
    case ProcessFrame(elapsedMs) =>
      // first handle all the messages events I received
      processQueue(eventQueue toList)
      nextFrame(elapsedMs)
      sender ! FrameProcessed
    case GetImplementation => sender ! this
    //case Move(xx, yy, zz) => internalMove(xx, yy, zz)
    //case x => handleMessage(x)
    case x =>
      eventQueue += x
      processQueue(eventQueue toList) // TODO: make this conditional only if I am not animating, in order to make all events being handled before frame to synchronize the time (if it is needed)
  }

  def processQueue(queue: List[Any]): Unit = {
    queue match {
      case Nil => eventQueue.clear
      case Move(xx, yy, zz) :: tail =>
        internalMove(xx, yy, zz)
        processQueue(tail)
      case head :: tail =>
        handleMessage(head)
        processQueue(tail)
    }
  }

  def internalMove(xx: Float, yy: Float, zz: Float) = {
    positionChanged = true
    x = xx; y = yy; z = zz
    updateTransformationMatrix
    onMove
  }

  /**
   * Invoked wherever a next frame is about to rendered, the entity should do all the stuff it wants at this point
   * @param elapsedMs - miliseconds elapsed since last frame was rendered
   */
  def nextFrame(elapsedMs: Long)
  /**
   * Invoked whenever a message is being sent to this entity
   */
  def handleMessage: PartialFunction[Any, Unit]
  override def move(x: Float, y: Float, z: Float) = self ! Move(x, y, z)
  def await[T: Manifest](msg: Any) = Await.result(self ? msg mapTo manifest[T], 10 seconds)

}