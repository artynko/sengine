package game.entity

import core.engine.entity.StaticEntity
import core.engine.entity.Rendered
import core.render.Mesh
import core.engine.entity.UiElement2D
import core.render.Textured
import game.entity.weapon.Weapon
import game.entity.skill.Skill
import game.entity.skill.SuppresingFire
import game.entity.skill.SuppresingFire
import core.engine.entity.EntityFactory
import game.entity.skill.EquipItem
import game.entity.skill.EquipHeavyMg

class HeavyMg(hasSkills: Boolean) extends Weapon {
  val _equipedSkills = if (hasSkills) List[Skill](EntityFactory.create[SuppresingFire]) else List[Skill]()
  val _notEquipedSkills = if (hasSkills) {
    val equip = EntityFactory.create[EquipHeavyMg]
    equip.weapon = this
    List[Skill](equip)
  } else List[Skill]()

  def equipedSkills = _equipedSkills
  def notEquipedSkills = _notEquipedSkills
  def skills = equipedSkills ++ notEquipedSkills
  def create = new HeavyMg(true)
  val meshName = "heavy_mg"
  val textureName = "textures/heavy_mg.png"
  base.iniciative.value = -2
  base.movement.value = -1
  base.range.value = 35
  base.rangedDmg.value = 5
  base.movementPenalty.value = 100

  val inventoryScale = 90f
  val inventoryXOffset = 145f
  val inventoryYOffset = 40f

}