package game.entity

import core.engine.entity.StaticEntity
import core.engine.entity.Rendered
import core.render.Mesh
import core.engine.entity.UiElement2D
import core.render.Textured
import game.entity.weapon.Weapon
import core.engine.entity.EntityFactory
import game.entity.skill.Skill
import game.entity.skill.RapidFire
import game.entity.skill.RapidFire
import game.entity.skill.SuppresingFire
import game.entity.skill.RapidFire

class AssaultRifle(hasSkills: Boolean) extends Weapon {
  
  val rapidFire = EntityFactory.create[RapidFire]
  val _equipedSkills = List[Skill](rapidFire)
  val _notEquipedSkills = List[Skill]()

  def equipedSkills = _equipedSkills
  def notEquipedSkills = _notEquipedSkills
  def skills = equipedSkills ++ notEquipedSkills
  def create = new AssaultRifle(true)
  val meshName = "asault_rifle"
  val textureName = "textures/assault_rifle.png"
  base.meeleDefense.value = 1
  base.meeleDmg.value = 2
  base.range.value = 45 
  base.rangedDmg.value = 3 
  base.movementPenalty.value = 30
  
  val inventoryScale = 120f
  val inventoryXOffset = 125f
  val inventoryYOffset = 40f

}