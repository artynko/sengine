package core.jgml.utils

import com.hackoeur.jglm.Vec3

/**
 * An convince factory class for jgml vec3
 */
object SVec3 {
  def apply(x: Float, y: Float, z: Float) = new Vec3(x, y, z);
}