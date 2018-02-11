package core.opengl

import scala.Array.canBuildFrom
import com.jogamp.common.nio.Buffers
import core.engine.CameraService
import core.engine.LightningService
import core.engine.entity.Entity
import core.render.Guid
import javax.media.opengl.GL2ES3
import javax.media.opengl.GL3
import javax.media.opengl.GLAutoDrawable
import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Matrices
import core.jgml.utils.SVec3
import core.engine.entity.UiElement2D

object MainProgram2D extends GLProgramMatcher {
  def matchMe(e: Entity) = e.isInstanceOf[Guid with UiElement2D]

}

class MainProgram2D extends MainProgram {
  override def enableProgram(gl: GL3, glDrawable: GLAutoDrawable) = {
    gl.glUseProgram(program)
    gl.glUniform3fv(diffuseLightDirectionLocation, 1, SVec3(6 * 100, 10 * 100, 5 * 100).getBuffer());
    val width = glDrawable.getWidth()
    val height = glDrawable.getHeight()
    val ortho2d = Matrices.ortho(0.0f, width, 0, height, -100, 100)
    val ubo2d = new Mat4(1.0f).getBuffer().array() ++ ortho2d.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo2d))
  }
}