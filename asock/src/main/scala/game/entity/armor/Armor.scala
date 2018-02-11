package game.entity.armor

import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import com.hackoeur.jglm.Vec3
import core.engine.entity.Clickable
import core.engine.entity.Entity
import core.engine.entity.EntityFactory
import core.engine.entity.Rendered
import core.engine.loaders.ModelLoader
import core.engine.loaders.ObjModelLoader
import core.jgml.utils.Glm
import core.jgml.utils.SVec3
import core.render.Mesh
import core.render.Textured
import game.entity.HasStats
import game.entity.InventoryItem
import game.entity.ui.inventory.InventorySlot
import game.entity.ui.inventory.ItemSlot
import game.entity.weapon.Weapon
import core.render.Border
import core.engine.EventBus
import game.entity.messages.EntityRemovedFromSlot
import game.entity.character.Character

trait Armor extends Mesh with Textured with Clickable with InventoryItem with HasStats {
  val eventBus = injectActor[EventBus]
  var character: Character = _
  val slot = collection.mutable.Map[Int, (Entity, Mat4, List[Vec3], ItemSlot)]()
  val rotationSpeed = 0.01f / 16.0f
  override def y_=(yy: Float) = super.y = yy + 0.05f

  // file location, posx, posy, sizex, sizey
  def slotConfig(config: List[(String, Int, Int, Int, Int)]) = {
    config zip (0 until config.size) foreach {
      case (c, n) =>
        val is = EntityFactory.create[InventorySlot]
        is.size(c._2, c._3, c._4, c._5)
        if (!c._1.contains("effect"))
          is.zIndex = 1
        is.hide
        is.slotId = n
        loadSlot(ObjModelLoader("models/" + c._1, 1), n, is) // a slot model is just a triangle
    }
  }

  def loadSlot(loader: ModelLoader, slotId: Int, is: InventorySlot) = {
    val data = loader.loadModel
    val p1 = SVec3(data._1(0), data._1(1), data._1(2))
    val p2 = SVec3(data._1(13), data._1(14), data._1(15))
    val p3 = SVec3(data._1(26), data._1(27), data._1(28))
    val topDownVector = p1.subtract(p2)
    slot(slotId) = (null, new Mat4(Glm.top(Matrices.lookAt(p1, p3, topDownVector))).transpose(), List(p1, p2, p3), is)
  }

  def addToSlot(slotId: Int, entity: Entity) = {
    if (!slot.contains(slotId))
      throw new RuntimeException("trying to assing item into slot " + slotId)
    slot transform {
      case (id, (e, mat, baseVertex, is)) if id == slotId =>
        entity match {
          case wpn: Weapon if slotId != 0 => // weapons add stats only if in slot 0 (i.e. equiped)
          case _ => registerModifier(entity)
        }
        entity match {
          case r: InventoryItem =>
            is.removeContent
            is.addContent(r)
          case _ =>
        }
        if (e != null) {
          unregisterModifier(e)
          // send an event that something was removed from slot
          eventBus.send(EntityRemovedFromSlot(slotId, character, e))
        }
        (entity, mat, baseVertex, is)
      case (id, (e, mat, baseVertex, is)) => (e, mat, baseVertex, is)
    }
    onUpdateTransformationMatrix
  }

  def removeFromSlot(entity: Entity) = {
    slot transform {
      case (id, (e, mat, baseVertex, is)) if e == entity =>
        entity match {
          case r: InventoryItem => is.removeContent // TODO: all should be inventory item..
          case _ =>
        }
        unregisterModifier(e)
        // send an event that something was removed from slot
        
        eventBus.send(EntityRemovedFromSlot(id, character, e))
        (null, mat, baseVertex, is)
      case (id, (e, mat, baseVertex, is)) => (e, mat, baseVertex, is)
    }
    onUpdateTransformationMatrix
  }

  def unequipAllItems = {
    println("removeing all")
    slot.toList.foreach {
      case (id, (e, mat, baseVertex, is)) => removeFromSlot(e)
    }

  }

  override def hide() = {
    super.hide
    slot foreach {
      case (_, (null, _, _, _)) => // nothign is in the slot, don't spam the message
      case (_, (r: Rendered, _, _, _)) => r.hide
      case (_, (e, _, _, _)) => println("can't hide something in slots" + e)
    }
  }

  override def onUpdateTransformationMatrix() = {
    slot foreach {
      case (id, (entity, mat, baseVertex, _)) if entity != null =>
        val p1 = baseVertex(0)
        val p2 = baseVertex(1)
        val p3 = baseVertex(2)
        val topDownVector = p1.subtract(p2)
        val myRotation = new Mat4(Glm.top(Matrices.lookAt(p1, p3, topDownVector))).transpose
        entity.transformationMatrix = new Mat4(1.0f).multiply(transformationMatrix).translate(p1).multiply(mat)
      case _ =>
    }
  }

  override def destroy() = {
    super.destroy
    slot foreach {
      case (id, (entity, mat, baseVertex, _)) if entity != null => entity.destroy
      case _ =>
    }
  }

}