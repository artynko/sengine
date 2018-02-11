package game.screen

import scala.math.abs
import scala.math.toRadians
import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import core.engine.CameraService
import core.engine.entity.Clickable
import core.engine.entity.DynamicEntity
import core.engine.messages.Clicked
import core.jgml.utils.SVec3
import core.jgml.utils.SVec4
import core.render.Textured
import game.core.Lifecycle
import game.entity.ArmorVest
import game.entity.AssaultRifle
import game.entity.Backpack
import game.entity.CombatTurtleneck
import game.entity.Grenade
import game.entity.HeavyMg
import game.entity.Pistol
import game.entity.Shotgun
import game.entity.ui.Label
import game.entity.ui.Rectangle
import game.repository.CharacterRepository
import game.user.input.Inventory
import game.user.input.Selection
import core.engine.messages.Moved
import game.entity.messages.CharacterSelected
import core.engine.SceneService
import game.scene._
import core.render.RenderingCore
import core.engine.EventBus
import scala.util.Success
import scala.util.Failure
import game.entity.messages.LoadScene
import scala.concurrent.ExecutionContext.Implicits.global
import game.entity.messages.ItemUnequiped
import core.engine.messages.KeyPressed
import game.entity.weapon.Weapon

class SquadEquipScreen extends DynamicEntity {
  val characterRepository = inject[CharacterRepository]
  val cameraService = inject[CameraService]
  val inventory = inject[Inventory]
  val warehouseWindow = injectActor[Warehouse]
  val sceneService = injectActor[SceneService]
  val eventBus = inject[EventBus]

  val screenLabel = new Label
  screenLabel.text = "Equip the team!"
  screenLabel.size = 40
  screenLabel.color = SVec4(1, 1, 1, 1)
  // this will be loaded by the "save" mechanism
  val shotgun = new Shotgun(false)
  warehouseWindow += (shotgun, 10)
  shotgun.hide
  val ar = new AssaultRifle(false)
  warehouseWindow += (ar, 10)
  ar.hide
  val hmg = new HeavyMg(false)
  warehouseWindow += (hmg, 10)
  hmg.hide
  val pistol = new Pistol
  warehouseWindow += (pistol, 10)
  pistol.hide
  val grenade = new Grenade(false)
  warehouseWindow += (grenade, 10)
  grenade.hide
  val backpack = new Backpack
  warehouseWindow += (backpack, 10)
  backpack.hide

  val combatTurtleNeck = new CombatTurtleneck
  warehouseWindow += (combatTurtleNeck, 10)
  combatTurtleNeck.hide

  val vest = new ArmorVest
  warehouseWindow += (vest, 10)
  vest.hide

  var currentSelected = 0
  var currentRotation = 0f
  var destinationRotation = -20f // I start -10 degress to the left

  val buttons = 0 until 7 map {
    case n =>
      val r = new Rectangle(0, 0, 100, 100) with Textured with Clickable {
        val textureName = "textures/test.png"
      }
      r.hide
      (r, n)
  }

  def distance = 360.0 / characterRepository().size

  def nextFrame(elapsedMs: Long): Unit = {
    characterRepository() zip (0 until characterRepository().size) foreach {
      case (ch, n) =>
        ch.armor.transformationMatrix = new Mat4(1.0f).multiply(Matrices.rotate(toRadians(distance * n - currentRotation).toFloat, SVec3(0, 1, 0)).translate(SVec3(0, -1, 4))).multiply(Matrices.rotate(toRadians(180).toFloat, SVec3(0, 1, 0)))
        ch.armor.onUpdateTransformationMatrix
    }
    val move = elapsedMs.toFloat / 2
    destinationRotation - currentRotation match {
      case 0f =>
      case _ if abs(destinationRotation - currentRotation) <= move =>
        currentRotation = destinationRotation // i.e. next move is more then is actually needed
        println(inventory)
        inventory.show(characterRepository(currentSelected))
        warehouseWindow.show
      case n if n < 0 => currentRotation = currentRotation - move
      case n if n > 0 => currentRotation = currentRotation + move
    }

    //cameraService.rotation = -toRadians(currentRotation).toFloat
    Selection.selected = Some(characterRepository(currentSelected))
    buttons zip (0 until characterRepository.ch.size) foreach {
      case ((b, n), _) =>
        val start = (Lifecycle.screenWidth / 2) - (characterRepository.ch.size * 120) / 2
        b.move(start + n * 120, 100, 0)
        b.show
    }
    screenLabel.move(Lifecycle.screenWidth / 2 - 150, Lifecycle.screenHeight - 100, 0)
  }

  var load = false
  def handleMessage = {
    case ItemUnequiped(_, _, item) =>
      item match {
        case w: Weapon => w.skills.foreach { skill =>
          skill.icon.destroy
          skill.destroy
        }
        case _ =>
      }
      item.destroy
    case Moved((ee, m, x, y)) => buttons foreach {
      // moved over myself
      case (b, n) if b == ee => characterRepository(n).self ! Moved((characterRepository(n).armor), m, x, y)
      case _ =>
    }
    case Clicked(Some(e), button, _) =>
      buttons foreach {
        case (b, n) if e == b && currentSelected != n => notSelectedButtonClicked(n)
        case (b, n) if e == b && currentSelected == n => selectedButtonClicked(n)
        case _ =>
      }
    case CharacterSelected(c) =>
      val n = characterRepository.ch.indexOf(c)
      currentSelected == n match {
        case true => selectedButtonClicked(n)
        case false => notSelectedButtonClicked(n)
      }
    case KeyPressed(keys) =>
      keys foreach {
        case "s" =>
          sceneService.unsetScene
          inventory.hide
          sceneService.unloadScene(SquadEquipScene)
          sceneService.unloadScene('characters)
          sceneService.loadScene(Loading)
          sceneService.switchScene(TacticalMissionScene, () => Unit)
        case _ =>
      }
    case msg =>
  }

  private def notSelectedButtonClicked(n: Int): Unit = {
    currentSelected = n
    inventory.hide
    warehouseWindow.hide
    destinationRotation = distance.toFloat * n - 20
    cameraService.rotation = 0
  }

  private def selectedButtonClicked(n: Int): Unit = {
    destinationRotation = distance.toFloat * n - 20
    cameraService.rotation = 0
  }
}