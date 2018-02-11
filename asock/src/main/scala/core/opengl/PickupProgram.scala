package core.opengl

import javax.media.opengl.GL3
import javax.media.opengl.GLAutoDrawable
import core.engine.CameraService
import core.engine.LightningService
import javax.media.opengl.GL2ES3
import com.jogamp.common.nio.Buffers
import core.engine.entity.Entity
import core.render.Guid

class PickupProgram extends GLProgram {
  val pickupDivider = 128
  val pickupTh: Float = 1f / pickupDivider
  val lightningService = inject[LightningService]
  val cameraService = inject[CameraService]
  var pickupColorLocation: Int = _

  def bindProgram(gl: GL3): Unit = {
    bindProgram(gl, "shaders/shader.vert", "shaders/pickup.frag")
    pickupColorLocation = gl.glGetUniformLocation(program, "pickupColor")
  }

  override def renderEntity(gl: javax.media.opengl.GL3, entity: Entity) = {
    val guid = entity.asInstanceOf[Guid].guid
    val mod: Int = guid % pickupDivider
    val div = ((guid - mod) / pickupDivider).toInt
    gl.glUniform3f(pickupColorLocation, div * pickupTh, mod * pickupTh, 0)
    super.renderEntity(gl, entity)
  }

  def renderEntityWithColor(gl: javax.media.opengl.GL3, entity: Entity, x: Float, y: Float, z: Float) = {
    gl.glUniform3f(pickupColorLocation, x, y, z)
    super.renderEntity(gl, entity)
  }

  def enableProgram(gl: GL3, glDrawable: GLAutoDrawable) = {
    gl.glUseProgram(program)
    val ubo = cameraService.camera.getBuffer().array() ++ cameraService.mat.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo))
  }

  def disableProgram(gl: GL3) = {

  }

}