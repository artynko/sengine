package core.engine

import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import com.hackoeur.jglm.Vec3
import com.hackoeur.jglm.Vec4

import core.app.Component
import core.jgml.utils.SVec3
import core.jgml.utils.SVec4

class CameraService extends Component {
  val cameraPan = injectActor[CameraPan]
  val display = inject[Display]

  cameraPan.cameraService = this
  var perspective: Mat4 = Matrices.perspective(70.0f, 640 / 480, 0.5f, 100.0f);
  def result = perspective multiply camera
  var rotation = 0.0f
  var distance = -27f
  var xRotation = 0.50f
  var zRotation = 0.0f
  var xOffset = 0.0f
  var xOffsetRotation = 0.0f
  var yOffsetRotation = 0.0f
  var zOffsetRotation = 0.0f
  var height = 0f
  private var _center = (0f, 0f)
  var storedSettings = Array[Float](xRotation, rotation, zRotation, distance, xOffset, height) 
  
  def push() = storedSettings = Array[Float](xRotation, rotation, zRotation, distance, xOffset, height) 
  def pop() = {
    xRotation = storedSettings(0)
    rotation = storedSettings(1)
    zRotation = storedSettings(2)
    distance = storedSettings(3)
    xOffset = storedSettings(4)
    height = storedSettings(5)
  }

  def setupPerspective(width: Int, height: Int) = {
    perspective = Matrices.perspective(45.0f, (width / height.toFloat), 0.5f, 500.0f)
  }
  def center(x: Float, z: Float) = _center = (x, z)
  def center = _center
  
  def cameraWorldSpace() = {
    val cameraWorldPos = new Mat4(1.0f).translate(SVec3(_center._1, 0, -_center._2)).multiply(Matrices.rotate(rotation, SVec3(0, 1, 0)).multiply(Matrices.rotate(xRotation, SVec3(1, 0, 0)))).translate(SVec3(0, 0, distance))
     val pos = cameraWorldPos.getColumn[Vec4](3) 
     SVec3(pos.getX(), pos.getY(), -pos.getZ())
  } 

  def posOnScreenFromWorldPos(worldPos: Vec3): Point  = {
    val v = new Vec4(worldPos, 1.0f).multiply(mat)
    val t = v.multiply(1 / v.getW()).add(SVec4(1, 1, 1, 0)).multiply(0.5f)
    Point(t.getX() * display.width toInt, t.getY() * display.height toInt)
  }

  /**
   * Returns the resulting camera matrix (i.e. perspective * camera location)
   */
  def mat(): Mat4 = result

  def camera = {
    new Mat4(1.0f)
      .multiply(Matrices.rotate(xOffsetRotation, SVec3(1, 0, 0)))
      .multiply(Matrices.rotate(yOffsetRotation, SVec3(0, 1, 0)))
      .multiply(Matrices.rotate(zOffsetRotation, SVec3(0, 0, 1)))
      .translate(SVec3(xOffset, 0, distance))
      .multiply(Matrices.rotate(zRotation, SVec3(0, 0, 1)))
      .multiply(Matrices.rotate(xRotation, SVec3(1, 0, 0)))
      .multiply(Matrices.rotate(rotation, SVec3(0, 1, 0)))
      .translate(SVec3(-_center._1, height, -_center._2))
  }
}