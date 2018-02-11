package core.render.texture

import core.jgml.utils.SVec3
import com.hackoeur.jglm.Vec3
import core.render.AlphaTexture

trait InMemoryTexture extends AlphaTexture {
  val alphaTextureName = "textures/default_alpha.bmp"
  val textureWidth: Int
  val textureHeight: Int
  val textureData: Array[Float] 
  def texturePointColor(x: Int, y: Int, color: Vec3) = {
    textureData((y * textureWidth * 3) + (x * 3)) = color.getX
    textureData((y * textureWidth * 3) + (x * 3) + 1) = color.getY
    textureData((y * textureWidth * 3) + (x * 3) + 2) = color.getZ
  }
}