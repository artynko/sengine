package core.app

import scala.reflect.ClassTag
import scala.reflect.Manifest
import akka.actor.Actor
import core.engine.EngineCore
import core.engine.entity.DynamicEntity
import core.opengl.OpenGLService
import core.render.RenderingCore
import core.engine.entity.EntityFactory
import core.engine.ActorFactory

object ApplicationContext {
  private var ctx: ApplicationContext = null;
  def get = ctx
}

/**
 * Wires together the dependecies for our applications
 */
class ApplicationContext {
  println("app context created")
  var objects = Map[Class[_], Any]();
  ApplicationContext.ctx = this;

  def getInstance[T](clazz: Class[T]): T = {
    //println("get " + clazz)
    objects.get(clazz) match {
      case Some(n) => 
        n.asInstanceOf[T]
      case None => instantiate(clazz)
    }
  }
  
  def register(any: Any) = objects += any.getClass -> any

  def instantiate[T](clazz: Class[T]): T = {
    println("instantiate " + clazz);
    val newInstance = clazz.newInstance();
    objects += clazz -> newInstance
    newInstance
  }
  
  // some dependecy injection bs here
  def instantiateAndinjectAll(profile: String) = {
    /*
    objects += classOf[OpenGLService] -> new OpenGLService;
    objects += classOf[RenderingCore] -> new RenderingCore;
    objects += classOf[EngineCore] -> new EngineCore;
    */
  };
  
  def injectActor[T <: Actor](implicit clazz: ClassTag[T]): T = {
    objects.get(clazz.erasure) match {
      case Some(i) => i.asInstanceOf[T]
      case None => 
        println("actor instance: " + clazz)
        val m = ActorFactory.create[T]
        objects += clazz.erasure -> m
        m.asInstanceOf[T]
    }
  }

  def inject[T: ClassTag](implicit clazz: ClassTag[T]): T = {
    return getInstance(clazz.erasure).asInstanceOf[T];
  };

}