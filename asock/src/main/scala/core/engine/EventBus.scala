package core.engine

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.Actor
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import core.app.Component
import core.engine.messages.GetImplementation

case class AddEvent(event: Any)

class EventBus extends Actor with Component {
  implicit val timeout = Timeout(1000 second)
  val events = collection.mutable.ListBuffer[Any]()

  def receive = {
    case AddEvent(event) => events += event
    case 'retrieve =>
      sender ! (events toList)
      events.clear
    case GetImplementation => sender ! this
  }
  
  def send(event: Any) = self ! AddEvent(event)
  def retrieve(): List[Any] = {
    val f = self ? 'retrieve mapTo manifest[List[Any]]
    Await.result(f, 5 seconds)
  }
}