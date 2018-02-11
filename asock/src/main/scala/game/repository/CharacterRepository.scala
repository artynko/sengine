package game.repository

import game.entity.character.Character

class CharacterRepository {
  val ch = collection.mutable.ListBuffer[Character]()
  
  def +=(newCharacter: Character) = ch += newCharacter
  def apply() = ch toList
  def apply(n: Int) = ch(n)

}