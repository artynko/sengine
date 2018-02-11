package core.engine

import com.hackoeur.jglm.Vec3
import core.jgml.utils.SVec3

class LightningService {
  var diffuseLight: Option[Vec3] = None
  
  def enableDiffuseLight(x: Float, y: Float, z: Float) = diffuseLight = Some(SVec3(x, y, z))
	
}