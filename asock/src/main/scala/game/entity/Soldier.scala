package game.entity

import core.engine.entity.Entity
import core.render.Mesh
import core.engine.entity.Clickable
import core.engine.messages.Clicked
import core.engine.entity.Rendered
import game.user.input.Selection
import core.engine.messages.Move
import core.engine.CameraService
import com.hackoeur.jglm.Matrices
import com.hackoeur.jglm.Vec3
import core.engine.entity.StaticEntity
import core.engine.entity.DynamicEntity
import game.user.input.TargetTiles
import core.jgml.utils.SVec3
import com.hackoeur.jglm.Mat4
import core.jgml.utils.Glm
import core.engine.messages.KeyPressed
import game.user.input.Inventory
import core.engine.messages.MoveOrigin
import core.render.PlyLoader
import game.entity.messages.AddEntityToSlot
import game.entity.messages.AddEntityToSlot
import core.engine.loaders.ObjModelLoader
import com.hackoeur.jglm.Mat3
import core.engine.loaders.ModelLoader

object Soldier {
  val SLOT_GUN = 0
  val SLOT_HIP = 1
  val SLOT_BACK = 2
  val SLOT_GUN_EFFECT = 3
}

class Soldier extends StaticEntity with Mesh with Clickable with Rendered {
  val meshName = "soldier_pretty3"
  val slot = collection.mutable.Map[Int, (Entity, Mat4, List[Vec3])]()
  loadSlot(ObjModelLoader("models/nsoldier_gun_slot.obj", 1), Soldier.SLOT_GUN)
  loadSlot(ObjModelLoader("models/nsoldier_hip_slot.obj", 1), Soldier.SLOT_HIP)
  loadSlot(ObjModelLoader("models/nsoldier_back_slot.obj", 1), Soldier.SLOT_BACK)
  loadSlot(ObjModelLoader("models/nsoldier_hip_gun_effect.obj", 1), Soldier.SLOT_GUN_EFFECT)
  val rotationSpeed = 0.01f / 16.0f
  override def y_=(yy: Float) = super.y = yy + 0.05f

  def loadSlot(loader: ModelLoader, slotId: Int) = {
    val data = loader.loadModel
    val p1 = SVec3(data._1(0), data._1(1), data._1(2))
    val p2 = SVec3(data._1(13), data._1(14), data._1(15))
    val p3 = SVec3(data._1(26), data._1(27), data._1(28))
    val topDownVector = p1.subtract(p2)
    slot(slotId) = (null, new Mat4(Glm.top(Matrices.lookAt(p1, p3, topDownVector))).transpose(), List(p1, p2, p3))
  }

  def addToSlot(slotId: Int, entity: Entity) = {
    if (!slot.contains(slotId))
      throw new RuntimeException("trying to assing item into slot " + slotId)
    slot transform {
      case (id, (e, mat, baseVertex)) if id == slotId => (entity, mat, baseVertex)
      case (id, (e, mat, baseVertex)) => (e, mat, baseVertex)
    }
  }

  override def onMove = {
    if (Selection.bar != null) Selection.bar.move(x, 1, z)
    slot foreach {
      case (id, (entity, mat, baseVertex)) if entity != null =>
        val p1 = baseVertex(0)
        val p2 = baseVertex(1)
        val p3 = baseVertex(2)
        val topDownVector = p1.subtract(p2)
        val myRotation = new Mat4(Glm.top(Matrices.lookAt(p1, p3, topDownVector))).transpose
        entity.transformationMatrix = new Mat4(1.0f).multiply(positionMatrix).multiply(rotationMatrix).translate(p1).multiply(mat)
      case _ =>
    }
  }
}