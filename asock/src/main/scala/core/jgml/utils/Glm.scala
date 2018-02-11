package core.jgml.utils

import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Mat3

object Glm {
  // takes the top 3 rows and columns from mat4 and returns them as mat3
  def top(mat4: Mat4): Mat3 = {
    val ar = mat4.getBuffer().array()
    val zipped = 0 to ar.length zip ar flatMap ({
      case (i, v) if i > 10 => List()
      case (i, v) if i % 4 < 3 => List(v)
      case _ => List()
    })
    new Mat3(zipped.toArray)
  }
}