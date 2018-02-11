package game.entity

import core.engine.entity.StaticEntity
import core.engine.entity.Rendered
import core.render.Mesh
import core.engine.entity.UiElement2D
import core.render.Textured
import game.entity.weapon.Thrown
import game.entity.skill.Skill
import core.engine.entity.EntityFactory
import game.entity.skill.GrenadeThrow

class Grenade(hasSkills: Boolean) extends Thrown {
  val _skills = if (hasSkills) {
    val g = EntityFactory.create[GrenadeThrow]
    g.item = this
    List[Skill](g)
  } else List[Skill]()

  def skills = _skills

  def create = new Grenade(true)
  val meshName = "grenade"
  val textureName = "textures/test.png"

  val inventoryScale = 170f
  val inventoryXOffset = 25f
  val inventoryYOffset = 30f
}