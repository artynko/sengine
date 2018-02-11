package core.opengl

import java.nio.ByteBuffer
import java.nio.IntBuffer

import scala.io.Source

import core.app.Component
import javax.media.opengl.GL
import javax.media.opengl.GL2ES2
import javax.media.opengl.GL3

/**
 * Provides various useful functions for opengl features
 */
class OpenGLService extends Component {

  // returns pointer to the OpenGL object
  def createShader(gl: GL3, filePath: String, shaderType: Int): Int = {
    val fragmentShader = gl.glCreateShader(shaderType);
    val shaderSource = Source.fromFile(filePath).getLines.mkString("\n")
    //println(shaderSource.toString())
    gl.glShaderSource(fragmentShader, 1, Array(shaderSource), null);
    gl.glCompileShader(fragmentShader);
    val b = IntBuffer.allocate(1);

    gl.glGetShaderiv(fragmentShader, GL2ES2.GL_COMPILE_STATUS, b);

    if (b.get(0) == GL.GL_FALSE) {
      val log = ByteBuffer.allocate(1000);
      gl.glGetShaderInfoLog(fragmentShader, 1000, null, log);
      println("Fragment shader compilation failed");
      println(new String(log.array()));
    }
    return fragmentShader;
  }

  def createProject(gl: GL3, shaderObjects: List[Int], uniforms: collection.mutable.Map[Int, (Int, Int, Int)]): Int = {
    val program = gl.glCreateProgram()
    shaderObjects foreach (gl.glAttachShader(program, _))
    gl.glLinkProgram(program)
    // retrieve the shared uniforms
    uniforms += program -> (
      gl.glGetUniformLocation(program, "perspectiveMatrix"),
      gl.glGetUniformLocation(program, "normalModelToCamera"),
      gl.glGetUniformLocation(program, "modelToWorld"))
    // bind the samplers regardless of what kind of program this is
    gl.glUseProgram(program)
    gl.glUniform1i(gl.glGetUniformLocation(program, "textureSampler"), 0);
    gl.glUniform1i(gl.glGetUniformLocation(program, "alphaSampler"), 1);
    gl.glUseProgram(0)
    program
  }

}