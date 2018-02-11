package core.opengl

import core.engine.entity.Entity
import core.render.Textured
import core.engine.entity.Rendered
import core.render.Guid
import core.render.AlphaTexture
import core.render.NonCulled
import core.app.Component
import core.render.RenderingCore
import javax.media.opengl.GL
import com.jogamp.common.nio.Buffers
import javax.media.opengl.GL2ES2
import javax.media.opengl.GL3
import scala.reflect.ClassTag
import javax.media.opengl.GLAutoDrawable
import core.render.NoDepthTest
import core.render.Border
import game.entity.AssaultRifle
import core.render.texture.InMemoryTexture
import com.hackoeur.jglm.Mat4
import core.jgml.utils.SVec4

object GLProgram {
  var lastVaoId = -1
  var currentTexture = ""
  var currentAlphaTexture = ""
  var cullingEnabled = true
  var depthTestEnabled = true
  var renderedVertices = 0
  var lastProgramId = -1
}

trait GLProgramMatcher {
  type T
  def unapply(e: Entity) = matchMe(e) match {
    case true => Some(e)
    case false => None
  }
  def matchMe(e: Entity): Boolean
}

trait GLProgram extends Component {
  val renderingCore = inject[RenderingCore]
  val glService = inject[OpenGLService]
  var modelWorldMatrixId = 0
  var program = 0

  def bindProgram(gl: GL3): Unit

  protected def bindProgram(gl: javax.media.opengl.GL3, vertShader: String, fragShader: String): Unit = {
    val vs = glService.createShader(gl, vertShader, GL2ES2.GL_VERTEX_SHADER)
    val fs = glService.createShader(gl, fragShader, GL2ES2.GL_FRAGMENT_SHADER)
    val map = collection.mutable.Map[Int, (Int, Int, Int)]()
    program = glService.createProject(gl, List(vs, fs), map)
    modelWorldMatrixId = map(program)._3 // TODO fix this bs
    gl.glUniformBlockBinding(program, gl.glGetUniformBlockIndex(program, "GlobalMatrices"), 0)
  }

  def smartEnable(gl: GL3, glDrawable: GLAutoDrawable) = GLProgram.lastProgramId match {
    case n if n == program =>
    case _ =>
      GLProgram.lastProgramId = program
      enableProgram(gl, glDrawable)
  }

  def disableProgram(gl: GL3)
  protected def enableProgram(gl: GL3, glDrawable: GLAutoDrawable)

  def renderEntity(gl: javax.media.opengl.GL3, entity: Entity) = {
    entity match {
      case e: Rendered if !e.rendered => // if it is a instance of rendered and rendered is not true do nothing
      case e: Entity with Guid =>
        // check if I need to bind the dao
        if (e.guid == 0)
         // println("no guid: " + e)
          renderingCore.setupVao(e)
        if (e.guid > 0 && e.renderData != null)
        try {
          if (GLProgram.lastVaoId != e.renderData.vaoId) {
            //println("last " + GLProgram.lastVaoId + " binding " + e.renderData.vaoId)
            gl.glBindVertexArray(e.renderData.vaoId)
            GLProgram.lastVaoId = e.renderData.vaoId
          }
          e match {
            case eb: Border =>
              gl.glClear(GL.GL_STENCIL_BUFFER_BIT); // clear stencil buffer for entities that have border
              gl.glEnable(GL.GL_STENCIL_TEST)
            case _ =>
          }
          e match {
            case en: InMemoryTexture with Guid if "tex_" + en.guid  != GLProgram.currentTexture =>
              gl.glActiveTexture(GL.GL_TEXTURE0);
              gl.glBindTexture(GL.GL_TEXTURE_2D, renderingCore.textureId("tex_" + en.guid))
              GLProgram.currentTexture = "tex_" + en.guid
            case en: Textured if en.textureName != GLProgram.currentTexture =>
              gl.glActiveTexture(GL.GL_TEXTURE0);
              gl.glBindTexture(GL.GL_TEXTURE_2D, renderingCore.textureId(en.textureName))
              GLProgram.currentTexture = en.textureName
            case _ =>
          }
          e match {
            case en: AlphaTexture if en.alphaTextureName != GLProgram.currentAlphaTexture =>
              gl.glActiveTexture(GL.GL_TEXTURE1);
              gl.glBindTexture(GL.GL_TEXTURE_2D, renderingCore.textureId(en.alphaTextureName))
              GLProgram.currentAlphaTexture = en.alphaTextureName
            case _ =>
          }
          e match {
            case ec: NonCulled if !GLProgram.cullingEnabled => // culling is disabled and I am supposed to be non cullled ignore
            case ec: NonCulled if GLProgram.cullingEnabled =>
              gl.glDisable(GL.GL_CULL_FACE)
              GLProgram.cullingEnabled = false
            case _ if !GLProgram.cullingEnabled =>
              gl.glEnable(GL.GL_CULL_FACE)
              GLProgram.cullingEnabled = true
            case _ =>
          }
          e match {
            case nd: NoDepthTest if !GLProgram.depthTestEnabled =>
            case nd: NoDepthTest if GLProgram.depthTestEnabled =>
              gl.glDisable(GL.GL_DEPTH_TEST)
              GLProgram.depthTestEnabled = false
            case _ if !GLProgram.depthTestEnabled =>
              gl.glEnable(GL.GL_DEPTH_TEST)
              GLProgram.depthTestEnabled = true
            case _ =>
          }
          // setup the model space position matrix
          gl.glUniformMatrix4fv(modelWorldMatrixId, 1, false, entity.transformationMatrix.getBuffer())
          GLProgram.renderedVertices += e.renderData.indexesLength / 3


          //println(this + " rendering" + entity + "\ta: " + GLProgram.currentAlphaTexture + " " + GLProgram.currentTexture + " indexes length " + e.renderData.indexesLength);
          gl.glDrawElements(GL.GL_TRIANGLES, e.renderData.indexesLength, GL.GL_UNSIGNED_INT, 0 * Buffers.SIZEOF_INT);
        } catch {
          case exception: Throwable =>
            println(e)
            throw exception
        }
      case _ => throw new RuntimeException("Can't render " + entity)
    }
  }
}