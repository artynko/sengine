package game.user.input

import core.app.Component
import core.engine.CameraService
import game.entity.ui.BasicWindow
import core.engine.messages.ZoomAndOffset
import core.jgml.utils.SVec4
import game.entity.InventoryItem
import game.core.Lifecycle
import game.entity.armor.Armor
import core.engine.SceneService
import game.scene.TacticalMissionScene
import game.scene.TacticalMissionScene

/**
 * Allows updating of an open inventory
 */
class Inventory extends Component {
  println("inventory created")
  val cameraService = inject[CameraService]
  val sceneService = injectActor[SceneService]
  var oldDistance = cameraService.distance
  var oldHeight = cameraService.height
  var oldXOffsetRotation = cameraService.xOffsetRotation
  var oldYOffsetRotation = cameraService.yOffsetRotation
  var oldZOffsetRotation = cameraService.zOffsetRotation
  var oldXOffset = cameraService.xOffset
  var visible = false
  var currentChar: game.entity.character.Character = null
  val inst: BasicWindow = new BasicWindow(300, 300, 384, 512, "Inventory")
  inst.borderColor(SVec4(0.7f, 0.7f, 0.7f, 0.5f))
  inst.headerColor(SVec4(0.95f, 0.95f, 0.8f, 0.7f))
  inst.color(SVec4(0.8f, 0.85f, 0.8f, 0.7f))
  inst.labelSize(16)
  inst.hide

  def equipArmor(item: Armor) = {
    currentChar.equipArmor(item)
    // refresh the inventory screen
    hide
    show(currentChar)
  }

  def addToSlot(slotId: Int, item: InventoryItem) = {
    println(this)
    currentChar.addToSlot(slotId, item)
  }

  def hide() = {
    println(this)
    if (visible) {
      inst.hide
      currentChar.armor.slot.foreach {
        case (id, (entity, mat, baseVertex, is)) => is.hide
        println(is)
      }
      //Selection.bar.rendered = true
      //Selection.effect.rendered = true
      //TargetTiles.list foreach (_.rendered = true)
      if (sceneService.currentScene == TacticalMissionScene) {
      //  cameraService.pop
      }
      visible = false
    }
  }
  def show(char: game.entity.character.Character) = {
    // setup slots
    if (!visible) {
      currentChar = char
      inst.clear
      println(char.armor.slot.size)
      char.armor.slot.foreach {
        case (id, (entity, mat, baseVertex, is)) =>
          is.show
          inst += is
          is.content match {
            case Some(c) => inst += c
            case None =>
          }
      }
      if (sceneService.currentScene == TacticalMissionScene) {
       // cameraService.push
        // handle camera
        //cameraService.cameraPan.self ! ZoomAndOffset(-0.05f, 0.8f, -5.5f, 0.8f)
      }
      //Selection.bar.rendered = false
      //Selection.effect.rendered = false
      //TargetTiles.list foreach (_.rendered = false)
      inst.show
      visible = true
    }
  }
}