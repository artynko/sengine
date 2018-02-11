package core.render.texture

import java.awt.image.DataBufferByte
import javax.imageio.ImageIO
import java.nio.ByteBuffer
import java.io.File
import com.hackoeur.jglm.Vec3
import core.jgml.utils.SVec3

class PngImage(path: String) {

  val image = ImageIO.read(new File(path))
  val textureIDList = new Array[Int](1)
  // image is a java.awt.image.BufferedImage (loaded from a PNG file)
  val data = image.getRaster.getDataBuffer match {
    case b: DataBufferByte => b.getData
    case _ => null
  }
  // PNG files are from top left to bottom right, and bytes are BGR, the byte values above 127 are encoded as -128 in integer form

  def pixelColorAt(x: Int, y: Int): Vec3 = {
    val pos = (x * image.getWidth() * 3) + (y * 3)
    // I am inverting R and B positions in here so I get it in the RGB format I use everywhere
    SVec3(convertToFloat(data(pos + 2)), convertToFloat(data(pos + 1)), convertToFloat(data(pos)))
  }
  
  def convertToFloat(v: Int): Float = {
    v match {
      case n if n >= 0 => n / 255f
      case n => (256 + n) / 255f // means we are in negative 128 is encoded as -128, 129 as -127 etc. until 255 is -1
    }
  }

}