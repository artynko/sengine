package core.opengl

import javax.media.opengl.GL3
import com.hackoeur.jglm.Mat4
import javax.media.opengl.GL2ES3
import com.hackoeur.jglm.Matrices
import com.jogamp.common.nio.Buffers
import javax.media.opengl.GLAutoDrawable
import core.render.FlatShader
import core.render.Guid
import core.engine.entity.Entity
import core.engine.entity.UiElement2D
import core.jgml.utils.SVec4
import core.render.Tinted
import core.engine.entity.Rendered

object FlatProgram2D extends GLProgramMatcher {
  def matchMe(e: Entity) = e.isInstanceOf[Guid with FlatShader with UiElement2D]
}

class FlatProgram2D extends FlatShaderProgram {
  var tintColorLocation: Int = _

  override def bindProgram(gl: GL3) = {
    super.bindProgram(gl)
    tintColorLocation = gl.glGetUniformLocation(program, "colorTint");
  }

  override def enableProgram(gl: GL3, glDrawable: GLAutoDrawable) = {
    gl.glUseProgram(program)
    val width = glDrawable.getWidth()
    val height = glDrawable.getHeight()
    val ortho2d = Matrices.ortho(0.0f, width, 0, height, -100, 100)
    val ubo2d = new Mat4(1.0f).getBuffer().array() ++ ortho2d.getBuffer().array()
    gl.glBufferSubData(GL2ES3.GL_UNIFORM_BUFFER, 0, 32 * Buffers.SIZEOF_FLOAT, Buffers.newDirectFloatBuffer(ubo2d))
  }

  override def renderEntity(gl: GL3, entity: Entity) {
    entity match {
      case e: Rendered with Tinted if e.rendered && e.tintColor.getW() > 0 =>
        gl.glUniform4f(tintColorLocation, e.tintColor.getX(), e.tintColor.getY(), e.tintColor.getZ(), e.tintColor.getW())
        super.renderEntity(gl, entity)
        gl.glUniform4f(tintColorLocation, 0f, 0f, 0f, 0f) // this is a possible performance hit, but I assume there won't be a high number of tinted objects
      case _ => super.renderEntity(gl, entity)

    }
  }

}