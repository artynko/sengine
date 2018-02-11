package core.engine

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import scala.reflect.ClassTag
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import core.engine.messages.GetImplementation

object ActorFactory {
  implicit val timeout = Timeout(1000 second)
  val system = ActorSystem("core-actors")
  def create[T <: Actor: ClassTag]: T = {
    val a = system.actorOf(Props[T])
    val f = a ? GetImplementation
    Await.result(f, 1000 second).asInstanceOf[T]
  }
  def create[T <: Actor: ClassTag](actorName: String): ActorRef = system.actorOf(Props[T], name = actorName)
  
}