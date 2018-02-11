package core.engine.entity

import scala.reflect.ClassTag
import core.engine.ActorFactory
import core.engine.messages.GetImplementation
import akka.actor.ActorRef
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future

object EntityFactory {
  implicit val timeout = Timeout(1000 second)
  def create[T <: DynamicEntity: ClassTag]: T = {
    val a = ActorFactory.create[T]
    a
  }
  def create[T <: DynamicEntity: ClassTag](name: String) = ActorFactory.create[T](name)
  
  def createStatic[T: ClassTag](implicit clazz: ClassTag[T]): T = clazz.erasure.newInstance().asInstanceOf[T]
}