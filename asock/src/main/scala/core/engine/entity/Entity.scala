package core.engine.entity

import com.hackoeur.jglm.Mat3
import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import com.hackoeur.jglm.Vec3
import core.app.Component
import core.engine.EngineCore
import core.jgml.utils.Glm
import core.jgml.utils.SVec3
import core.jgml.utils.SVec4

trait Entity extends Component {
  val engineCore = inject[EngineCore]
  var _x: Float = 0
  var _y: Float = -100
  var _z: Float = 0
  var scaleX: Float = 1.0f
  var scaleY: Float = 1.0f
  var scaleZ: Float = 1.0f
  var positionChanged = false
  var rotationMatrix = new Mat4(1.0f)
  var positionMatrix = new Mat4(1.0f)
  var transformationMatrix = new Mat4(1.0f)

  engineCore register this

  def scale_=(s: Float) = {
    scaleX = s
    scaleY = s
    scaleZ = s
    updateTransformationMatrix
  }

  def scale = scaleX // TODO: get rid of this if it poses an issue

  def updateTransformationMatrix() = {
    // try to do a "global scale"
    val globalScale = 1f
    val globalScaleMatrix = new Mat4(SVec4(globalScale, 0, 0, 0), SVec4(0, globalScale, 0, 0), SVec4(0, 0, globalScale, 0), SVec4(0, 0, 0, 1))
    val scaleMatrix = new Mat4(SVec4(scaleX, 0, 0, 0), SVec4(0, scaleY, 0, 0), SVec4(0, 0, scaleZ, 0), SVec4(0, 0, 0, 1))
    positionMatrix = new Mat4(1).multiply(globalScaleMatrix).translate(SVec3(x, y, z)).multiply(scaleMatrix)
    transformationMatrix = positionMatrix.multiply(rotationMatrix)
    onUpdateTransformationMatrix
  }

  def rotateY(r: Float) = {
    rotationMatrix = Matrices.rotate(r, SVec3(0, 1, 0))
    updateTransformationMatrix
  }
  
  /**
   * Gets my current position as Vec3
   */
  def position() = SVec3(x, y, z)
  
  def x = _x;
  def x_=(xx: Float) = _x = xx

  def y = _y;
  def y_=(yy: Float) = _y = yy

  def z = _z;
  def z_=(zz: Float) = _z = zz

  def move(xx: Float, yy: Float, zz: Float) = {
    x = xx
    y = yy
    z = zz
    updateTransformationMatrix
    onMove
  }
  def lookAt(origin: Vec3, target: Vec3, orientation: Vec3): Unit = {
    rotationMatrix = new Mat4(Glm.top(Matrices.lookAt(origin, target, orientation))).transpose()
    updateTransformationMatrix
  }
  def lookAt(entity: Entity): Unit = lookAt(SVec3(x, y, z), SVec3(entity.x, entity.y, entity.z), SVec3(0, 1, 0))

  def onMove() = {}
  def onUpdateTransformationMatrix = {}
  def destroy() = engineCore.unregister(this)

}