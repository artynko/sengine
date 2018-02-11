package game.entity.messages

import core.engine.entity.Entity
import game.entity.character.Character
import game.entity.InventoryItem
import game.entity.armor.Armor
import scala.concurrent.Future
import com.hackoeur.jglm.Vec3
import game.map.entity.SquadEntity

case class AddEntityToSlot(slotId: Int, entity: Entity) // internal for character
case class RemoveEntityFromSlot(entity: Entity) // internal for character
case class EntityRemovedFromSlot(slotId: Int, ch: Character, item: Entity) // used by armor to tell character something was removed
case class CharacterSelected(c: Character)
case class ItemUnequiped(c: Character, slotId: Int, item: InventoryItem) // global event when item is unequiped
case class LoadScene(sceneIdentifier: Any, startCommand: () => Future[Unit])
case class SquadMoved(squad: SquadEntity, pos: Vec3) // squad moved on global map
