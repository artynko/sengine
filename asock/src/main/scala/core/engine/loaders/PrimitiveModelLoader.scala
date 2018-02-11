package core.engine.loaders

import core.engine.entity.Entity
import core.engine.entity.primitive.PrimitiveBox2D
import core.engine.entity.primitive.PrimitiveGrid
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer

object PrimitiveModelLoader {
  def apply(entity: Entity) = new PrimitiveModelLoader(entity)
}

class PrimitiveModelLoader(entity: Entity) extends ModelLoader {

  def loadModel(): (Array[Float], Array[Int]) = {
    entity match {
      case e: PrimitiveBox2D =>
        val bottomLeftUvs = (e.bottomLeftUvs._1, e.bottomLeftUvs._2)
        val topRightUvs = (e.topRightUvs._1, e.topRightUvs._2)
        val vertices = Array(e.bottomLeft._1, e.bottomLeft._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), bottomLeftUvs._1, topRightUvs._2, // bottom left - 4 vertex, 3 normal, 4 color
          e.topRight._1, e.bottomLeft._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), topRightUvs._1, topRightUvs._2, // bottom right
          e.topRight._1, e.topRight._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), topRightUvs._1, bottomLeftUvs._2, // top right
          e.bottomLeft._1, e.topRight._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), bottomLeftUvs._1, bottomLeftUvs._2 // top left
          )
        /* reversed
        val vertices = Array(e.bottomLeft._1, e.bottomLeft._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), bottomLeftUvs._1, bottomLeftUvs._2,  // bottom left - 4 vertex, 3 normal, 4 color
          e.topRight._1, e.bottomLeft._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), topRightUvs._1, bottomLeftUvs._2, // bottom right
          e.topRight._1, e.topRight._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), topRightUvs._1, topRightUvs._2, // top right
          e.bottomLeft._1, e.topRight._2, e.zIndex, 1.0f, 0f, 0f, 1f, e.color.getX(), e.color.getY(), e.color.getZ(), e.color.getW(), bottomLeftUvs._1, topRightUvs._2 // top left
          )
          */
        val indexes = Array(0, 1, 2, 0, 2, 3)
        (vertices, indexes)

      case e: PrimitiveGrid =>
        val vertices = ListBuffer[Float]()
        val indexes = ListBuffer[Int]()
        for (rowNum <- 1 until e.vertexColors.size / e.width) {
          if (rowNum == 1) { // when starting the 0 row needs to be added first
            for (colNum <- 0 until e.width) {
              val color = e.vertexColors(colNum)
              vertices ++= List(colNum, 0, 0, // xyz
                1, 0, 0, 1, // normal
                color.getX(), color.getY(), color.getZ(), color.getW(), // color
                0, 0) // uvs
            }
          }
          // now add the current row, and update indexes
          for (colNum <- 0 until e.width) {
            val i = rowNum * e.width + colNum
            val color = e.vertexColors(i)
            vertices ++= List(colNum, rowNum, 0, // xyz
              1, 0, 0, 1, // normal
              color.getX(), color.getY(), color.getZ(), color.getW(), // color
              0, 0) // uvs
            if (colNum > 0) { // add the indexes
            	val bottomLeft = i - e.width - 1
            	val bottomRight = i - e.width
            	val topRight = i
            	val topLeft = i - 1
            	indexes ++= List(bottomRight, topRight, bottomLeft, bottomLeft, topRight, topLeft)
            }
          }
        }

        (vertices.toList.toArray, indexes.toList.toArray)
      case e => throw new RuntimeException("Can't create data for primitive" + e)
    }
  }

}