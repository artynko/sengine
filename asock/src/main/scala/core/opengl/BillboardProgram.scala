package core.opengl

import javax.media.opengl.GL3
import core.engine.CameraService
import javax.media.opengl.GL2ES3
import com.jogamp.common.nio.Buffers
import core.render.FlatShader
import core.render.Guid
import core.engine.entity.Entity
import javax.media.opengl.GL
import core.engine.entity.UiElement3D
import javax.media.opengl.GLAutoDrawable

object BillboardProgram extends GLProgramMatcher {
  def matchMe(e: Entity) = e.isInstanceOf[Guid with FlatShader with UiElement3D]
}

class BillboardProgram extends GLProgram {
  val cameraService = inject[CameraService]
  var modelToCameraLocation: Int = _

  def bindProgram(gl: GL3): Unit = {
    bindProgram(gl, "shaders/flat_3d.vert", "shaders/flat.frag")
    modelToCameraLocation = gl.glGetUniformLocation(program, "modelToCamerac")
  }

  def enableProgram(gl: GL3, glDrawable: GLAutoDrawable) = {
    gl.glUseProgram(program)
    val ubo = cameraService.camera.getBuffer().array() ++ cameraService.mat.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo))
    gl.glDisable(GL.GL_DEPTH_TEST)
    gl.glUniformMatrix4fv(modelToCameraLocation, 1, false, cameraService.camera.getBuffer())
  }

  def disableProgram(gl: GL3) = {
    gl.glEnable(GL.GL_DEPTH_TEST)
  }
}