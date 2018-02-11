package game.entity

import game.entity.character.Stats

trait HasStats {
  val _current = new Stats
  val _base = new Stats
  val modifiers = collection.mutable.ListBuffer[HasStats]()

  /**
   * Current stats
   */
  def current = _current;
  /**
   * Base stats, this item grants
   */
  def base = _base;
  
  /**
   * Gives a total stats this item + its modifiers grant
   */
  def max(): Stats = {
    val current = new Stats
    addStatModifier(current, base) // add the base stats of this item
    modifiers.foldLeft(current) {
      case (myStats, modifier) => 
        addStatModifier(myStats, modifier.max) // add the max stats all modifiers provide (i.e. a weapon can have its own modifiers (silencer, nozzle accelerator)
    }
  }

  /**
   * Sets all the combat stats current value to theirs max value
   */
  def updateCurrentCombatStats = {
    current.meeleDefense.value = max.meeleDefense.value
    current.meeleDmg.value = max.meeleDmg.value
    current.rangedDmg.value = max.rangedDmg.value
    current.range.value = max.range.value
  }

  def alive(): Boolean = current.hp.value > 0

  def registerModifier(s: Any) = s match {
    case e: HasStats => modifiers += e
    case _ =>
  }

  def unregisterModifier(s: Any) = s match {
    case e: HasStats => modifiers -= e
    case _ =>
  }

  private def addStatModifier(s: Stats, d: Stats): Stats = {
    s.iniciative.value += d.iniciative.value
    s.movement.value += d.movement.value
    s.meeleDmg.value += d.meeleDmg.value
    s.meeleDefense.value += d.meeleDefense.value
    s.hp.value += d.hp.value
    s.range.value += d.range.value
    s.rangedDmg.value += d.rangedDmg.value
    s.rangedDefense.value += d.rangedDefense.value
    s.movementPenalty.value += d.movementPenalty.value
    s.hitPenalty.value += d.hitPenalty.value
    s
  }
}