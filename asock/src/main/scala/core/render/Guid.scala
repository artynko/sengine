package core.render

/**
 * A trait that contains information about a rendering data
 */
case class RenderData(vaoId: Int, indexesLength: Int, vboId: Int, vboStart: Int)
trait Guid {
	var guid = 0
	var _renderData: RenderData = _
	def renderData = _renderData
	//def renderData_=(vaoId: Int, indexesLength: Int, vboId: Int, vboStart: Int) = RenderData(vaoId, indexesLength, vboId, vboStart)
	def renderData_=(renderData: RenderData) = _renderData = renderData

}