package core.engine

import scala.concurrent.Promise
import akka.actor.Actor
import akka.actor.actorRef2Scala
import core.app.Component
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import core.engine.entity.StaticEntity
import core.engine.entity.Text2D
import core.engine.entity.UiElement2D
import core.engine.messages.Clicked
import core.engine.messages.FrameProcessed
import core.engine.messages.GetEntities
import core.engine.messages.GetImplementation
import core.engine.messages.ProcessFrame
import core.engine.messages.RegisterEntity
import core.engine.messages.StartFrameProcessing
import core.engine.userinput.UserInputListener
import core.render.Guid
import core.engine.messages.KeyPressed
import core.engine.entity.Rendered
import core.engine.messages.UnregisterEntity
import core.engine.messages.Released
import core.engine.messages.Pressed
import core.engine.messages.Moved
import game.entity.AssaultRifle

private case class SetScene(any: Any)
private case class UnloadScene(any: Any)
private case class AddToScene(any: Any, entity: Entity)
private case class LoadScene(any: Any)
private case class DeleteScene(any: Any)

class EngineCoreActor extends Actor with Component {
  val userInputListener = inject[UserInputListener]

  private val sceneEntities = collection.mutable.Map[Any, Set[Entity]]()
  private var staticEntities = Set[StaticEntity]()
  private var entitiesList = Set[DynamicEntity]()
  private var entities2d = Set[Entity]()
  private var dynamic2d = Set[DynamicEntity]()
  var lastFrameMillis: Long = 0
  // stores a promise that once fullfilled will unblock the EngineCore 
  var entitiesSimulationDone: Promise[Unit] = null;
  var processingEntities = 0
  val eventBus = injectActor[EventBus]
  var currentScene: Option[Any] = None

  def receive = {
    case StartFrameProcessing(p) =>
      processingEntities = entities.size + dynamic2d.size
      entitiesSimulationDone = p
      processFrame()
    // last entity is done
    case FrameProcessed if processingEntities == 1 => entitiesSimulationDone success ()
    // not last entity is done
    case FrameProcessed => processingEntities -= 1
    case GetEntities => sender ! entities.toList
    case 'static => sender ! staticEntities.toList
    case 'dynamic => sender ! entitiesList.toList
    case 'entities2d => sender ! entities2d.toList
    case RegisterEntity(entity) => addEntity(entity)
    case UnregisterEntity(entity) =>
      removeEntity(entity)
      sceneEntities transform {
        case (k, v) => v filter (_ != entity) // this is complete destroy so we remove it also from all the scenes
      }
      //println(sceneEntities('characters))
      updateEntities
      sender ! 'success
    case AddToScene(sceneIdentifier, entity) => sceneEntities(sceneIdentifier) = sceneEntities.getOrElse(sceneIdentifier, Set[Entity]()) + entity
    case SetScene(sceneIdentifier) =>
      currentScene = Some(sceneIdentifier)
      println("set scene " + sceneIdentifier)
      sender ! 'success
    case 'unsetScene =>
      currentScene = None
      println("unsed scene")
      sender ! 'success
    case UnloadScene(sceneIdentifier) =>
      sceneEntities.getOrElse(sceneIdentifier, List[Entity]()) foreach (removeEntity(_))
      println("UNLOAD scene: '" + sceneIdentifier + "'")
      sender ! 'success
    case LoadScene(sceneIdentifier) =>
      sceneEntities.getOrElse(sceneIdentifier, List[Entity]()) foreach (addEntity(_))
      println("load scene" + sceneIdentifier)
      sender ! 'success
    case DeleteScene(sceneIdentifier: Any) => sceneEntities(sceneIdentifier) = Set[Entity]()
      println("DELETE scene: '" + sceneIdentifier + "'")
      sender ! 'success
    case 'sendEvents =>
      val events = eventBus.retrieve
      entities ++ dynamic2d foreach { entity =>
        for (event <- events)
          entity.self ! event
      }
    // make this synchronous
    case GetImplementation => sender ! this
    case msg => println("uknown message " + msg)
  }

  def entities: List[DynamicEntity] = entitiesList.toList

  def sendEvents() = self ! 'sendEvents
  def addToScene(sceneIdentifier: Any, entity: Entity) = self ! AddToScene(sceneIdentifier, entity)
  def setScene(sceneIdentifier: Any) = self ! SetScene(sceneIdentifier)
  def unloadScene(sceneIdentifier: Any) = self ! UnloadScene(sceneIdentifier)
  def loadScene(sceneIdentifier: Any) = self ! LoadScene(sceneIdentifier)

  def updateEntities() = {
    /*
    entitiesList = _entities flatMap ({
      case (k, v) => v
    }) toList*/
  }

  def removeEntity(entity: Entity) = {
    entities2d = entities2d filter (_ != entity)
    staticEntities = staticEntities filter (_ != entity)
    /*entity match {
      case e: DynamicEntity => _entities(entity.getClass()) = _entities.getOrElse(entity.getClass(), (Set[DynamicEntity]())) - e
      case _ =>
    }*/
    entitiesList = entitiesList filter (_ != entity)
    dynamic2d = dynamic2d filter (_ != entity)
    updateEntities
  }

  def addEntity(entity: Entity) = {
    internalAddToScene(entity)
    entity match {
      case e: Text2D => entities2d = entities2d + e
      case e: UiElement2D =>
        entities2d = entities2d + e
        // TODO: get rid of this once merged
        //add them also to dynamic entities if they are dynamic
        e match {
          case ee: DynamicEntity => dynamic2d = dynamic2d + ee
          case _ =>
        }
      case e: StaticEntity =>
        staticEntities = staticEntities + e
        updateEntities()
      case entity: DynamicEntity =>
        //_entities(entity.getClass()) = _entities.getOrElse(entity.getClass(), (Set[DynamicEntity]())) + entity
        entitiesList = entitiesList + entity
        updateEntities()
      case e: Entity => // don't do anything with normal entites as those are ignored by most of the processes
    }
  }

  def processFrame() = {
    //println("e: " +staticEntities.filter(_.isInstanceOf[AssaultRifle]))
    if (lastFrameMillis == 0)
      lastFrameMillis = System.currentTimeMillis();
    val currentMillis = System.currentTimeMillis();
    val elapsedMs = if (currentMillis - lastFrameMillis > 100) 100 else currentMillis - lastFrameMillis
    lastFrameMillis = currentMillis

    val clickedObjects = createInteractedList(userInputListener.clicked, () => userInputListener.clicked = None)
    val presssedObjects = createInteractedList(userInputListener.pressed, () => userInputListener.pressed = None)
    val releasedObjects = createInteractedList(userInputListener.released, () => userInputListener.released = None)
    val movedObjects = createInteractedList2(userInputListener.moved, () => userInputListener.moved = None)
    val keyPressed = userInputListener.keyPressed match {
      case Some(key) =>
        userInputListener.keyPressed = None
        List(key)
      case None => List[String]()
    }
    val events = eventBus.retrieve

    entities ++ dynamic2d foreach { entity =>
      if (keyPressed.size > 0)
        entity.self ! KeyPressed(keyPressed)
      /*for (guidModifier <- clickedObjects) // instead of this I send it as event from rendering core
        entity.self ! Clicked(guidModifier)*/
      for (guidModifier <- releasedObjects)
        entity.self ! Released(guidModifier)
      for (guidModifier <- presssedObjects)
        entity.self ! Pressed(guidModifier)
      for (guidModifier <- movedObjects)
        entity.self ! Moved(guidModifier)
      for (event <- events)
        entity.self ! event
      entity.self ! ProcessFrame(elapsedMs)
    }

  }

  private def createInteractedList(input: Option[(Int, Int)], cleanupFunc: () => Unit): List[(Entity, Int)] = {
    input match {
      case Some((guid, modifiers)) =>
        val r = (entities ++ staticEntities ++ entities2d).foldLeft(List[(Entity, Int)]())({
          case (clickedList, entity: Guid) if entity.guid == guid =>
            entity match {
              case e: Rendered if !e.rendered => clickedList // happens due to the antialiasing error
              case _ => (entity, modifiers) :: clickedList
            }
          case (clickedList, _) => clickedList
        })
        cleanupFunc()
        r
      case _ => List[(Entity, Int)]()
    }
  }

  // TODO: get rid of this bs
  private def createInteractedList2(input: Option[(Int, Int, Int, Int)], cleanupFunc: () => Unit): List[(Entity, Int, Int, Int)] = {
    input match {
      case Some((0, modifiers, x, y)) => List((null, modifiers, x, y))
      case Some((guid, modifiers, x, y)) =>
        val r = (entities ++ staticEntities ++ entities2d).foldLeft(List[(Entity, Int, Int, Int)]())({
          case (clickedList, entity: Guid) if entity.guid == guid =>
            entity match {
              case e: Rendered if !e.rendered => clickedList // happens due to the antialiasing error
              case _ => (entity, modifiers, x, y) :: clickedList
            }
          case (clickedList, _) => clickedList
        })
        cleanupFunc()
        r
      case _ => List[(Entity, Int, Int, Int)]()
    }
  }

  /**
   * Adds entity to scene
   */
  private def internalAddToScene(entity: core.engine.entity.Entity): Unit = {
    // handle scenes
    currentScene match {
      case Some(ident) => sceneEntities(ident) = sceneEntities.getOrElse(ident, Set[Entity]()) + entity
      case None =>
    }
  }
}