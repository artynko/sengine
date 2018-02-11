package game.app

import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import core.app.ApplicationContext
import core.app.Component
import core.engine.CameraService
import core.render.RenderingCore
import core.render.texture.PngImage
import game.entity.AssaultRifle
import game.entity.ui.InventoryDragAndDrop
import core.jgml.utils.SVec3

object TestApp extends App with Component {
  val ctx = new ApplicationContext()
  /*
  val renderingCore = inject[RenderingCore]
  val cameraService = inject[CameraService]
  injectActor[InventoryDragAndDrop]
  val ar = new AssaultRifle(false)
  cameraService.distance = -2
  cameraService.rotation = 0
  cameraService.xRotation = 0
  ar.move(0, 0, 0)
  ar.rotateY(toRadians(90).toFloat)
  renderingCore.start
  * 
  */

  /*
  val l = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  val buffer = IntBuffer.wrap(l.toArray)
  println(buffer.remaining())
  println(buffer)
  buffer.position(3)
  println(buffer.remaining())
  println(buffer)
  */

  val image = new PngImage("textures/512red.png")
  println(image.pixelColorAt(0, 0));
  println(image.pixelColorAt(0, 1));
  println(image.pixelColorAt(1, 0));
  println(image.pixelColorAt(1, 1));
  println(image.pixelColorAt(1, 2));
  
  
  val m = Matrices.lookAt(SVec3(1.3f, 2.2f, 89f), SVec3(1f, 3.5f, 2.2f), SVec3(5.3f, 0.2f, 12f)).translate(SVec3(0.5f, 2.3f, 4.8f))

  println(m) // m
  println(Matrices.inverse(m)) // inverse m
  println(Matrices.inverse(Matrices.inverse(m))) // m
  println(m.multiply(Matrices.inverse(m))) // identity
  
}