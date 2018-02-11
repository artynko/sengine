package core.engine.loaders

import scala.io.Source
import scala.collection.mutable.MutableList
import com.hackoeur.jglm.Vec3
import scala.collection.generic.MutableMapFactory
import scala.collection.mutable.LinkedList
import scala.collection.mutable.ListBuffer
import core.jgml.utils.SVec3

object PlyModelLoader {
  def apply(filePath: String): PlyModelLoader = {
    return new PlyModelLoader(filePath)
  }
}

/**
 * A loader object that loads a data about object from a OBJ file
 * structure is 4 floats vertex, 3 floats normal, 4 floats color
 */
class PlyModelLoader(filePath: String) extends ModelLoader {

  def loadModel(): (Array[Float], Array[Int]) = {
    val vertices = ListBuffer[Float]()
    val indexes = ListBuffer[Int]()
    val ElementVertex = "element vertex (.*)".r
    val ElementFace = "element face (.*)".r
    var vertexN = 0
    var vertexesDone = 0
    var faceN = 0
    var facesDone = 0
    var headerDone = false

    for (line <- Source.fromFile(filePath).getLines()) {
      line match {
        case ElementVertex(rest) => vertexN = rest.toInt
        case ElementFace(rest) => faceN = rest.toInt
        case "end_header" => headerDone = true
        case l if headerDone && vertexN > 0 && vertexesDone < vertexN =>
          val s = l.split(" ")
          // 4x vertices, 3x normal, 4x color
          vertices ++= List(s(0).toFloat, s(1).toFloat, s(2).toFloat, 1.0f, s(3).toFloat, s(4).toFloat, s(5).toFloat, s(6).toFloat / 256f, s(7).toFloat / 256f, s(8).toFloat / 256f, 1.0f)
          vertexesDone += 1
        case l if headerDone && faceN > 0 && facesDone < faceN =>
          val s = l.split(" ")
          indexes ++= List(s(1).toInt, s(2).toInt, s(3).toInt)
          faceN += 1
        case _ => 
      }
    }
    return (vertices.toArray, indexes.toArray);
  }
}