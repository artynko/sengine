package core.app

import core.engine.entity.DynamicEntity
import scala.reflect.ClassTag
import game.screen.Warehouse
import akka.actor.Actor

trait Component {
 def inject[T](implicit m: ClassTag[T]): T = {
    return ApplicationContext.get.inject[T]
  } 
 def injectActor[T <: Actor](implicit m: ClassTag[T]): T = {
    return ApplicationContext.get.injectActor[T]
  } 
}