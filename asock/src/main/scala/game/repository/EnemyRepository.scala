package game.repository

import core.app.Component
import game.entity.computer.character.MeleeAiCharacter

class EnemyRepository extends Component {
  val list = collection.mutable.ListBuffer[MeleeAiCharacter]()
  
  def +=(entity: MeleeAiCharacter) = list += entity
  def apply() = list toList
  def apply(n: Int) = list(n)

}