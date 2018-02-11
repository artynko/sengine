package game.entity.ui

import core.engine.entity.DynamicEntity
import core.engine.entity.EntityFactory
import com.hackoeur.jglm.Vec4
import core.jgml.utils.SVec4

private case class AddMessage(message: String)

object LogWindow {
  def apply(): LogWindow = EntityFactory.create[LogWindow]
}

class LogWindow extends DynamicEntity {

  val MAX_LINES = 10
  val lines = 0 until MAX_LINES map { n =>
    val l = EntityFactory.createStatic[Label]
    l.x = 50 
    l.y = 150 + ((n + 1) * 17)
    l.size = 14 
    l.color = SVec4(1, 1, 0.8f, 1)
    l
  }
  var content = 0 until MAX_LINES map (_ => "") toList

  def nextFrame(elapsedMs: Long) = {
    // update all ines with texts
    lines zip content foreach {
      case (label, text) =>
        label.text = text
    }
  }

  def handleMessage = {
    case AddMessage(msg) => content = (msg :: content) take MAX_LINES
    case _ =>
  }

  def add(s: String) = self ! AddMessage(s)

}