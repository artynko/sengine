package game.entity.weapon

import core.render.Border
import core.render.Mesh
import core.render.Textured
import game.entity.HasStats
import game.entity.InventoryItem
import game.entity.skill.Skill

trait Weapon extends InventoryItem with Mesh with Textured with HasStats {
	def equipedSkills: List[Skill]
	def notEquipedSkills: List[Skill]
  
}