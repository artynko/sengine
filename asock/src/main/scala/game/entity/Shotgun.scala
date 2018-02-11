package game.entity

import core.engine.entity.StaticEntity
import core.engine.entity.Rendered
import core.render.Mesh
import core.engine.entity.UiElement2D
import core.render.Textured
import game.entity.weapon.Weapon
import core.engine.entity.EntityFactory
import game.entity.skill.Skill
import game.entity.skill.SuppresingFire
import game.entity.skill.CrushingShot
import game.entity.skill.EquipShotgun

class Shotgun(hasSkills: Boolean) extends Weapon {
  val _equipedSkills = if (hasSkills) List[Skill](EntityFactory.create[CrushingShot]) else List[Skill]()
  val _notEquipedSkills = if (hasSkills) {
    val equip = EntityFactory.create[EquipShotgun]
    equip.weapon = this
    List[Skill](equip)
  } else List[Skill]()

  def equipedSkills = _equipedSkills
  def notEquipedSkills = _notEquipedSkills
  def skills = _equipedSkills ++ notEquipedSkills

  def create = new Shotgun(true)
  val meshName = "weapons/shotgun"
  val textureName = "textures/assault_rifle.png"
  base.meeleDefense.value = 2
  base.meeleDmg.value = 3
  base.range.value = 15
  base.rangedDmg.value = 6
  base.movementPenalty.value = 0

  val inventoryScale = 115f
  val inventoryXOffset = 128f
  val inventoryYOffset = 40f

}