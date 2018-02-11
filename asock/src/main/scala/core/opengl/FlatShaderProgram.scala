package core.opengl

import javax.media.opengl.GL3
import core.engine.CameraService
import javax.media.opengl.GL2ES3
import com.jogamp.common.nio.Buffers
import core.render.FlatShader
import core.render.Guid
import core.engine.entity.Entity
import javax.media.opengl.GLAutoDrawable

object FlatShaderProgram extends GLProgramMatcher {
  def matchMe(e: Entity) = e.isInstanceOf[Guid with FlatShader]
}

class FlatShaderProgram extends GLProgram {
  val cameraService = inject[CameraService]

  def bindProgram(gl: GL3): Unit = bindProgram(gl, "shaders/shader.vert", "shaders/flat.frag")

  def enableProgram(gl: GL3, glDrawable: GLAutoDrawable) = {
    gl.glUseProgram(program)
    val ubo = cameraService.camera.getBuffer().array() ++ cameraService.mat.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo))
  }

  def disableProgram(gl: GL3) = {

  }
}