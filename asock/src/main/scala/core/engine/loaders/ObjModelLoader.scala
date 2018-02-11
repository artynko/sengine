package core.engine.loaders

import scala.io.Source
import scala.collection.mutable.MutableList
import com.hackoeur.jglm.Vec3
import scala.collection.generic.MutableMapFactory
import scala.collection.mutable.LinkedList
import scala.collection.mutable.ListBuffer
import core.jgml.utils.SVec3

object ObjModelLoader {
  def apply(filePath: String, guid: Int): ObjModelLoader = {
    return new ObjModelLoader(filePath, guid)
  }
}

/**
 * A loader object that loads a data about object from a OBJ file
 * structure is 4 floats vertex, 3 floats normal, 4 floats color
 */
class ObjModelLoader(filePath: String, guid: Int) extends ModelLoader {

  def loadModel(): (Array[Float], Array[Int]) = {
    val vectors = ListBuffer[Vec3]()
    val normals = ListBuffer[Vec3]()
    val uvs = ListBuffer[(Float, Float)]()
    val mixed = ListBuffer[Float]()
    val indexes = ListBuffer[Int]()
    val vectorNormalMap = collection.mutable.Map[String, Integer]();
    val VertexLine = "v (.*)".r 
    val NormalLine = "vn (.*)".r
    val UvLine = "vt (.*)".r
    val GroupLine = "f (.*)".r
    for (line <- Source.fromFile(filePath).getLines()) {
      line match {
        case VertexLine(rest) =>
          val coords = rest.split(" ")
          vectors += SVec3(coords(0).toFloat, coords(1).toFloat, coords(2).toFloat);
        case NormalLine(rest) =>
          val coords = rest.split(" ")
          normals += SVec3(coords(0).toFloat, coords(1).toFloat, coords(2).toFloat);
        case UvLine(rest) =>
          val coords = rest.split(" ")
          uvs += ((coords(0).toFloat, coords(1).toFloat))
        case GroupLine(vertexNormalGroup) =>
          vertexNormalGroup.split(" ") foreach (vns => {
            val i = vectorNormalMap getOrElseUpdate (vns, {
              val currentIndex: Integer = vectorNormalMap.size;
              val i = vns.split("/")
              val vector = vectors(i(0).toInt - 1)
              val normal = normals(i(2).toInt - 1)
              val uv = uvs.size match {
                case 0 => List(0.0f, 0.0f)
                case _ => 
                  val r = uvs(i(1).toInt - 1)
                  List(r._1, -r._2)
              }
              mixed ++= List(vector.getX(), vector.getY(), vector.getZ(), 1f);
              mixed ++= List(normal.getX(), normal.getY(), normal.getZ());
              // TODO: implement proper color
              filePath match {
                case "models/nsoldier.obj" => mixed ++= List(0.0f, 0.106f, 0.324f, 1f);
                case "models/asault_rifle.obj" => mixed ++= List(0.1f, 0.1f, 0.124f, 1f);
                case "models/pistol.obj" => mixed ++= List(0.1f, 0.1f, 0.124f, 1f);
                case "models/grenade.obj" => mixed ++= List(0.1f, 0.2f, 0.124f, 1f);
                case "models/backpack.obj" => mixed ++= List(0.1f, 0.15f, 0.124f, 1f);
                case "models/heavy_mg.obj" => mixed ++= List(0.1f, 0.1f, 0.13f, 1f);
                case "models/tree1.obj" => mixed ++= List(0.2f, 0.1f, 0.01f, 1f);
                case "models/bar.obj" => mixed ++= List(1f, 0.0f, 0.0f, 1f);
                //case "models/tile.obj" => mixed ++= List(0.1f, 0.46f, 0.14f, 1f)
                case "models/grass.obj" => mixed ++= List(0.07f, 0.35f, 0.12f, 1f)
                case "models/tile.obj" => mixed ++= List(0.05f, 0.26f, 0.07f, 1f)
                case "models/tileTarget.obj" => mixed ++= List(0.3f, 0.3f, 0.0f, 0.15f)
                case "models/selection.obj" => mixed ++= List(0.9f, 0.9f, 0.0f, 0.3f);
                case _ => mixed ++= List(0.4f, 0.4f, 0.4f, 1f);
              }
              // UVs
              mixed ++= uv
              currentIndex;
            })
            indexes += i
          })
        case _ => 

      }
    }
    return (mixed.toArray, indexes.toArray);
  }
}