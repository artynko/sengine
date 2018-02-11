package game.entity.ui

import core.engine.CameraService
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import core.engine.entity.EntityFactory
import core.engine.messages.Moved
import core.jgml.utils.SVec3
import core.jgml.utils.SVec4
import game.service.CombatService
import game.entity.HasStats

/**
 * A popup used to show text on mouse over events
 */
class MouseOverPopup extends DynamicEntity {
  val cameraService = inject[CameraService]
  val combatService = inject[CombatService]

  val label = EntityFactory.createStatic[Label]
  label.size = 16
  label.x = 0
  label.y = 0
  label.hide
  label.color = SVec4(1, 1, 1, 1) // white
  val rangeLabel = EntityFactory.createStatic[Label]
  rangeLabel.size = 16
  rangeLabel.x = 0
  rangeLabel.y = 0
  rangeLabel.hide
  rangeLabel.color = SVec4(0, 1, 0, 1) // white

  var target: Option[Entity with HasStats] = _
  var attacker: Option[Entity with HasStats] = _

  def nextFrame(elapsedMs: Long) = {
    target match {
      case Some(t) if label.rendered =>
        // move him to his target
        val e = t
        val a = attacker.get
        val targetScreenPoint = cameraService.posOnScreenFromWorldPos(SVec3(e.x, e.y, e.z))
        // set the text and everything
        val currentRange = combatService.getDistance(a, e)
        val weaponRange = a.current.range.value
        val hit = combatService.computeHit(a, e)
        val popupText = "Hit:\nDamage: " + (-combatService.computeRangedDmg(a, e)) + "\nHP: " + e.current.hp.value
        label.move(targetScreenPoint.x + 30, targetScreenPoint.y - 20, 0)
        rangeLabel.move(targetScreenPoint.x + 60, targetScreenPoint.y - 20, 0)
        label.text = popupText
        hit match {
          case h if h == 0 => rangeLabel.color = SVec4(1, 0, 0, 1)
          case h if h == 100 => rangeLabel.color = SVec4(0, 1, 0, 1)
          case _ => rangeLabel.color = SVec4(1, 1, 0, 1)
        }
        rangeLabel.text = hit + s"% ($currentRange/$weaponRange)"
      case _ =>
    }
  }

  def handleMessage = {
    case Moved((ee, m, x, y)) if (ee == this) =>
    case msg =>
  }

  def setText(text: String) = this.label.text = text
  def setTarget(target: Entity with HasStats) = this.target = Some(target)
  /**
   * Sets both and shows the label
   */
  def setTargets(attacker: Entity with HasStats, defender: Entity with HasStats) = {
    setTarget(defender)
    this.attacker = Some(attacker)
    show
  }
  def clearTarget() = {
    target = None
    hide
  }
  override def show() = {
    label.show
    rangeLabel.show
  }
  override def hide() = {
    label.hide
    rangeLabel.hide
  }

}