package game.entity.ui

import core.render.Textured
import core.engine.entity.DynamicEntityFactory
import core.engine.entity.AlphaIndex
import core.engine.entity.Clickable
import core.engine.entity.EntityFactory
import core.jgml.utils.SVec4
import game.core.turn.TurnBased
import core.render.Border
import core.render.Tinted

class SkillIcon(bx: Int, by: Int, w: Int, h: Int, portraitIdX: Int, portraitIdY: Int) extends Rectangle(bx, by, w, h) with TurnBased with Clickable
  with Textured with DynamicEntityFactory[SkillIcon] with AlphaIndex with Tinted {
  
  val alphaIndex = 30
  val textureName = "textures/skill_icons.png"
  bottomLeftUvs = (portraitIdX * 0.25f, portraitIdY * 0.25f)
  topRightUvs = (portraitIdX * 0.25f + 0.25f, portraitIdY * 0.25f + 0.25f)

  var _cooldown: Int = _
  val label = EntityFactory.createStatic[Label]
  label.size = 56
  label.x = 0
  label.y = 0
  label.color = SVec4(1, 0, 0, 1) // white
  cooldown(0)

  def create = {
    val n = new SkillIcon(bx, by, w, h, portraitIdX, portraitIdY)
    n.guid = this.guid
    n
  }

  def myTurnStart() = {}
  def myTurnEnd() = {}
  def newTurn() = {
    _cooldown match {
      case 0 =>
      case _ => cooldown(_cooldown - 1)
    }
  }

  def cooldown(turns: Int) = {
    _cooldown = turns
    turns match {
      case 0 =>
        label.hide
      case t =>
        label.text = turns.toString
    }
  }

  override def onMove = {
    label.move(x + 15, y + 15, 0)
  }

  override def hide = {
    super.hide
    label.hide
  }

  override def show = {
    super.show
    if (_cooldown > 0)
      label.show
  }

}