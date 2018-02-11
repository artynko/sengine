package core.engine.loaders

import core.engine.entity.CompositeEntity
import core.render.Mesh
import com.hackoeur.jglm.Mat4
import com.hackoeur.jglm.Vec4
import com.hackoeur.jglm.Matrices
import core.jgml.utils.SVec3
import com.hackoeur.jglm.Vec3
import core.jgml.utils.Glm

object CompositeModelLoader {
    val vertexDataForMesh = collection.mutable.Map[String, (Array[Float], Array[Int])]()
}


class CompositeModelLoader(e: CompositeEntity) extends ModelLoader {

  def loadModel(): (Array[Float], Array[Int]) = {
    val staticVaoIndexArray = collection.mutable.ListBuffer[Int]()
    val staticVaoVerticesArray = collection.mutable.ListBuffer[Float]()

    for (entity <- e.entities) {
      entity match {
        case e: Mesh =>
          val data = CompositeModelLoader.vertexDataForMesh.getOrElseUpdate(e.meshName, {
            val modelLoader = ObjModelLoader("models/" + e.meshName + ".obj", 0)
            println("loading static mesh %s " format e.meshName)
            val data = modelLoader.loadModel
            println("done")
            data
          })
          // now I need to transform all of them
          //val modelSpacePosition = new Mat4(1.0f).translate(SVec3(entity.x, entity.y, entity.z)).multiply(entity.rotationMatrix)
          val modelSpacePosition = entity.transformationMatrix
          // transform all the vertex data so they are in correct place for the big static vao
          val t = for (m <- data._1.sliding(13, 13)) yield {
            val v = new Vec4(m(0), m(1), m(2), m(3))
            val n = new Vec4(m(4), m(5), m(6), 1.0f)
            val tv = v.multiply(modelSpacePosition)
            val tn = n.multiply(entity.rotationMatrix)
            List[Float](tv.getX(), tv.getY(), tv.getZ(), tv.getW(), tn.getX(), tn.getY(), tn.getZ(), m(7), m(8), m(9), m(10), m(11), m(12))
          }
          val staticVerticesSize = staticVaoVerticesArray.size / 13
          staticVaoVerticesArray ++= t flatMap (l => l)
          staticVaoIndexArray ++= data._2 map (_ + staticVerticesSize)
        case _ => throw new RuntimeException("Unknown model loader")
      }
    }
    (staticVaoVerticesArray.toArray, staticVaoIndexArray.toArray)
  }
}