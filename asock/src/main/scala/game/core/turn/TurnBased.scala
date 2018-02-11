package game.core.turn

/**
 * A trait that tells that an entity is turnbased
 */
trait TurnBased {
 def myTurnStart() 
 def myTurnEnd() 
 def newTurn()
}