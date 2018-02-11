package game.entity

import core.render.Textured
import game.entity.armor.Armor
import game.entity.skill.Skill

object CombatTurtleneck {
  val SLOT_GUN = 0
  val SLOT_HIP = 1
  val SLOT_BACK = 2
}

class CombatTurtleneck extends Armor with Textured {
  def skills = List[Skill]()
  def create = new CombatTurtleneck
  slotConfig(List(
    ("nsoldier_gun_slot.obj", 100, 350, 6, 3),
    ("nsoldier_hip_slot.obj", 100, 260, 2, 2),
    ("nsoldier_back_slot.obj", 50, 50, 3, 6),
    ("nsoldier_hip_gun_effect.obj", 348, 0, 1, 1)))
  override val meshName = "soldier_pretty3"
  val textureName = "textures/test.png"
  base.iniciative.value = 8
  base.movement.value = 9
  base.hp.value = 9
  base.meeleDefense.value = 1

  val inventoryScale = 40f
  val inventoryXOffset = 100f
  val inventoryYOffset = 5f
}