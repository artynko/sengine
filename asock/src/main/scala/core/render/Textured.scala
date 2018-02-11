package core.render

trait Textured extends AlphaTexture {
  val textureName: String
  val alphaTextureName = "textures/default_alpha.bmp"
}