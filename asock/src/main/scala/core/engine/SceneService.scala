package core.engine

import scala.concurrent
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import core.app.Component
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import core.render.RenderingCore
import game.scene.SquadEquipScene

class SceneService extends DynamicEntity {
  val renderingCore = inject[RenderingCore]
  val eventBus = injectActor[EventBus]
  var reload = 0
  var beforeReload: Option[() => Unit] = None
  var nextScene: Any = _

  /**
   * Isn't in fact current but last activated
   */
  var currentScene: Any = _

  def setScene(sceneIdentifier: Any) = {
    currentScene = sceneIdentifier
    Await.result(engineCore.inst.self ? SetScene(sceneIdentifier), 30 seconds)
  }
  def unsetScene() = Await.result(engineCore.inst.self ? 'unsetScene, 30 seconds)
  def unloadScene(sceneIdentifier: Any) = Await.result(engineCore.inst.self ? UnloadScene(sceneIdentifier), 30 seconds)
  def addToScene(sceneIdentifier: Any, entity: Entity) = engineCore.inst.addToScene(sceneIdentifier, entity)
  def loadScene(sceneIdentifier: Any) = {
    currentScene = sceneIdentifier
    Await.result(engineCore.inst.self ? LoadScene(sceneIdentifier), 30 seconds)
  }
  def deleteScene(sceneIdentifier: Any) = Await.result(engineCore.inst.self ? DeleteScene(sceneIdentifier), 30 seconds)
  def switchScene(sceneIdentifier: Any, beforeReload: () => Unit) = {
    reload = 120 
    nextScene = sceneIdentifier
    this.beforeReload = Some(beforeReload)
    self ! 'reload // should reload asynchronously while the scene is still rendering
  }

  def nextFrame(elapsedMs: Long) = {
    self ! 'reload
  }
  def handleMessage = {
    case 'reload =>
      beforeReload match {
        case Some(cmd) if reload > 0 => reload -= 1
        case Some(cmd) if reload == 0 =>
          cmd() // execute whatever we want before reload
          renderingCore.pauseAndSwitch onComplete { // then pause and switch to new scene
            case Success(restartCmd) => eventBus.send(game.entity.messages.LoadScene(nextScene, restartCmd)) // load the squad equip scene
            case Failure(e) => throw new RuntimeException("switch failed" + e)
          }
          beforeReload = None // nothing to reload now
        case None =>
      }
    case msg => 
  }

}