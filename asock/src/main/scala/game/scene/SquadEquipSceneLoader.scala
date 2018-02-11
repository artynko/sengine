package game.scene

import core.engine.entity.DynamicEntity
import core.engine.SceneService
import game.entity.ui.StatsPanel
import core.engine.CameraService
import game.screen.SquadEquipScreen
import game.repository.CharacterRepository
import core.engine.entity.EntityFactory
import game.entity.ui.InventoryDragAndDrop
import game.user.input.Selection
import game.entity.messages.LoadScene
import game.user.input.Inventory
import akka.dispatch.sysmsg.Failed
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import game.entity.Shotgun
import core.render.RenderingCore
import core.jgml.utils.SVec4

/**
 * Loads the squad equip scene
 */
class SquadEquipSceneLoader extends DynamicEntity {
  val sceneService = injectActor[SceneService]
  val cameraService = inject[CameraService]
  val chRepo = inject[CharacterRepository]
  sceneService.setScene(SquadEquipScene)
  val m = injectActor[SquadEquipScreen]
  injectActor[InventoryDragAndDrop]
  injectActor[StatsPanel]
  sceneService.unloadScene(SquadEquipScene) // unload it I only want it to get loaded when the LoadScene event is send
  sceneService.unsetScene
  val renderingCore = inject[RenderingCore]
  val inventory = inject[Inventory]

  /**
   * Invoked wherever a next frame is about to rendered, the entity should do all the stuff it wants at this point
   * @param elapsedMs - miliseconds elapsed since last frame was rendered
   */
  def nextFrame(elapsedMs: Long) = {}
  /**
   * Invoked whenever a message is being sent to this entity
   */
  def handleMessage: PartialFunction[Any, Unit] = {
    case LoadScene(SquadEquipScene, startCmd) =>
      sceneService.unloadScene(sceneService.currentScene)
      sceneService.unsetScene
      sceneService.loadScene('characters)
      sceneService.loadScene(SquadEquipScene)
      renderingCore.updateBackgroundColor(SVec4(0, 0.0f, 0.0f, 1))
      // select something from the chRepo
      // now set the scene back to characters as everything that will be created during equipment should belong to them
      startCmd() onComplete {
        case Success(s) =>
          chRepo().headOption match {
            case Some(ch) => Selection.selected = Some(ch)
            case None =>
          }
        //  inventory.show(Selection.selected.get)
          cameraService.center(0, 0)
          cameraService.xRotation = 0.0f
          cameraService.distance = -10
          inventory.show(Selection.selected.get)
          sceneService.setScene('characters)

        case Failure(f) =>
      }
    case _ => 
  }

}