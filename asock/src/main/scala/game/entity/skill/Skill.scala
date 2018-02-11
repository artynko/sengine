package game.entity.skill

import core.engine.entity.DynamicEntity
import game.entity.HasStats
import core.engine.entity.Entity
import game.core.turn.TurnBased
import game.entity.ui.SkillIcon
import core.engine.EventBus

case class SkillUsed(skill: Skill, entity: Entity)
object Skill {
  val ICON_SIZE = 65
}
abstract class Skill extends DynamicEntity with TurnBased {
  protected val eventBus = injectActor[EventBus]
  
  def icon(): SkillIcon

}