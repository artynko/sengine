package game.entity.ui

import core.app.RegisteringComponent
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import game.core.Lifecycle

case class RegisterPortrait(entity: Entity, col: Int, row: Int)
case class UpdatePanel(entity: List[Entity])

class IniciativePanel extends DynamicEntity with RegisteringComponent {

  val portraitMap = collection.mutable.Map[Entity, IniciativePortrait]()
  var currentPortraits = List[IniciativePortrait]()
  val current = collection.mutable.ListBuffer[IniciativePortrait]();

  var n = 0;

  def nextFrame(elapsedMs: Long) = {
    currentPortraits zip (0 until currentPortraits.size) foreach {
      case (portrait, n) => portrait.move(20, Lifecycle.screenHeight - 160 - n * 90, 0)
    }
  }

  def handleMessage = {
    case RegisterPortrait(e, col, row) =>
      val p = new IniciativePortrait(0, 0, 80, 80, col, row)
      p.hide
      portraitMap(e) = p
    case UpdatePanel(entities) => // update the panel with new entities
      //println(s"entities $entities")
      current foreach (_.destroy)
      current.clear
      currentPortraits = entities zip (0 until entities.size) map {
        case (entity, n) =>
          val portrait = portraitMap(entity)
          val tmp = portrait.create
          tmp.show
          current += tmp
          tmp
      }
      portraitMap foreach {
        case (k, v) => println(v.guid)
      }
      println(this + "current portraints" + currentPortraits)
    case msg =>
  }

  def registerPortrait(e: Entity, col: Int, row: Int) = self ! RegisterPortrait(e, col, row)
  def updatePanel(entities: List[Entity]) = self ! UpdatePanel(entities)

}