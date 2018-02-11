package game.entity

import game.entity.weapon.Weapon
import game.entity.skill.Skill

class Pistol extends Weapon {
  val _equipedSkills = List[Skill]()
  val _notEquipedSkills = List[Skill]()

  def equipedSkills = _equipedSkills
  def notEquipedSkills = _notEquipedSkills
  def skills = List[Skill]()
  def create = new Pistol
  val textureName = "textures/assault_rifle.png"
  val meshName = "pistol"

  val inventoryScale = 130f
  val inventoryXOffset = 50f
  val inventoryYOffset = 30f

}