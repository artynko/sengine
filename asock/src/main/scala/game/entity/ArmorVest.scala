package game.entity

import game.entity.armor.Armor
import game.entity.skill.Skill

object ArmorVest {
  val SLOT_GUN = 0
  val SLOT_HIP = 1
  val SLOT_BACK = 2
  def apply() = new ArmorVest
}

class ArmorVest extends Armor {
  def skills = List[Skill]()
  def create = ArmorVest()
  val meshName = "armor_vest"
  val textureName = "textures/test.png"
  slotConfig(List(
    ("nsoldier_gun_slot.obj", 100, 350, 6, 3),
    ("nsoldier_hip_slot.obj", 100, 260, 2, 2),
    ("nsoldier_back_slot.obj", 50, 50, 3, 6),
    ("nsoldier_hip_gun_effect.obj", 300, 100, 1, 1),
    ("armor_vest_belt_slot.obj", 220, 260, 2, 2)))
  base.iniciative.value = 7
  base.movement.value = 8
  base.hp.value = 10
  base.meeleDefense.value = 2
  base.rangedDefense.value = 2

  val inventoryScale = 40f
  val inventoryXOffset = 100f
  val inventoryYOffset = 5f

}