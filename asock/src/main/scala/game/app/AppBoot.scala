package game.app

import scala.concurrent
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import core.app.ApplicationContext
import core.app.Component
import core.engine.CameraPan
import core.engine.CameraService
import core.engine.EventBus
import core.engine.SceneService
import core.engine.entity.AlphaIndex
import core.engine.entity.Entity
import core.engine.entity.EntityFactory
import core.engine.entity.UiElement2D
import core.engine.entity.primitive.PrimitiveBox2D
import core.jgml.utils.SVec4
import core.render.RenderingCore
import game.core.turn.TurnManager
import game.core.turn.TurnManager
import game.core.turn.TurnManager
import game.entity.AssaultRifle
import game.entity.Backpack
import game.entity.Bar
import game.entity.CombatTurtleneck
import game.entity.Grass
import game.entity.GrassTileModel
import game.entity.Grenade
import game.entity.GunFireEffect
import game.entity.GunFireEffect
import game.entity.HeavyMg
import game.entity.Pistol
import game.entity.SelectionEfect
import game.entity.Shotgun
import game.entity.TileTarget
import game.entity.TilesBlock
import game.entity.TilesBlock
import game.entity.TilesBlockFlat
import game.entity.TilesBlockGrass
import game.entity.Tree1
import game.entity.character.Character
import game.entity.computer.character.MeleeAiCharacter
import game.entity.ui.IniciativePanel
import game.entity.ui.IniciativePortrait
import game.entity.ui.InventoryDragAndDrop
import game.entity.ui.Label
import game.entity.ui.Rectangle
import game.entity.ui.StatsPanel
import game.entity.ui.inventory.ItemSlot
import game.map.generator.TileContainer
import game.repository.CharacterRepository
import game.scene._
import game.screen.SquadEquipScreen
import game.entity.messages.LoadScene
import game.repository.EnemyRepository
import game.entity.Soldier
import game.entity.ui.LogWindow
import game.user.input.Inventory
import core.engine.EngineCore
import game.scene.GlobalMapSceneLoader

object AppBoot extends App with Component {
  val ctx = new ApplicationContext()
  val renderingCore = inject[RenderingCore]
  val sceneService = injectActor[SceneService]
  val eventBus = injectActor[EventBus]
  val chRepo = inject[CharacterRepository]
  val enemyRepository = inject[EnemyRepository]
  val cameraService = inject[CameraService]
  val turnManage = injectActor[TurnManager]


  sceneService.setScene('characters)
  val log = injectActor[LogWindow]
  // create the characters
  0 until 3 foreach { _ => // create 4 characters for the character repository
    val ch = Character()
    ch.equipArmor(new CombatTurtleneck)
    ch.addToSlot(0, new AssaultRifle(true))
    ch.assignPortrait(0, 0)
    chRepo += ch
  }
  sceneService.unloadScene('characters)
  sceneService.unsetScene

  // instantiate the sceneLoaders
  injectActor[TacticalMissionLoader]
  injectActor[SquadEquipSceneLoader]
  injectActor[GlobalMapSceneLoader]

  sceneService.setScene(Loading)
  // loading text
  val loading = new Label
  loading.text = "Loading..."
  loading.x = 100
  loading.y = 100
  loading.size = 36
  loading.color = SVec4(1, 1, 1, 1)
  sceneService.unsetScene

  renderingCore.swapInterval = 0
  renderingCore.showFpsCounter
  renderingCore.start

  sceneService.switchScene(GlobalMapScene, () => Unit)

}