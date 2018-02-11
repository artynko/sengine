package core.opengl

import javax.media.opengl.GL3
import core.engine.LightningService
import core.engine.CameraService
import javax.media.opengl.GL2ES3
import com.jogamp.common.nio.Buffers
import core.engine.entity.Entity
import core.render.Guid
import javax.media.opengl.GLAutoDrawable
import core.render.Tinted
import core.engine.entity.Rendered

object MainProgram extends GLProgramMatcher {
  def matchMe(e: Entity) = e.isInstanceOf[Guid]
  
}

class MainProgram extends GLProgram {
  val lightningService = inject[LightningService]
  val cameraService = inject[CameraService]
  var diffuseLightDirectionLocation: Int = _
  var tintColorLocation: Int = _

  override def renderEntity(gl: GL3, entity: Entity) {
    entity match {
      case e: Rendered with Tinted if e.rendered && e.tintColor.getW() > 0 =>
        gl.glUniform4f(tintColorLocation, e.tintColor.getX(), e.tintColor.getY(), e.tintColor.getZ(), e.tintColor.getW())
        super.renderEntity(gl, entity)
        gl.glUniform4f(tintColorLocation, 0f, 0f, 0f, 0f) // this is a possible performance hit, but I assume there won't be a high number of tinted objects
      case _ => super.renderEntity(gl, entity)

    }
  }

  def bindProgram(gl: GL3): Unit = {
    bindProgram(gl, "shaders/shader.vert", "shaders/shader.frag")
    diffuseLightDirectionLocation = gl.glGetUniformLocation(program, "diffuseLightDirection");
    tintColorLocation = gl.glGetUniformLocation(program, "colorTint");
  }

  def enableProgram(gl: GL3, glDrawable: GLAutoDrawable) = {
    gl.glUseProgram(program)
    lightningService.enableDiffuseLight(6, 10, 5)
    lightningService.diffuseLight match {
      case Some(n) => gl.glUniform3fv(diffuseLightDirectionLocation, 1, n.getBuffer());
      case None => throw new RuntimeException("No diffuse light specified")
    }
    val ubo = cameraService.camera.getBuffer().array() ++ cameraService.mat.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo))
  }

  def disableProgram(gl: GL3) = {

  }

}