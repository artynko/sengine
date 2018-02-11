package game.entity.character

class Stats {
	val movement = new Stat()
	movement.name = "Movement"
	movement.value = 0

	val iniciative = new Stat()
	iniciative.value = 0

	val hp = new Stat()
	hp.value = 0

	val meeleDmg = new Stat()
	meeleDmg.value = 0
	
	val meeleDefense = new Stat()
	meeleDefense.value = 0

	val rangedDmg = new Stat()
	rangedDmg.value = 0
	
	val range = new Stat()
	range.value = 0
	
	val rangedDefense = new Stat()
	rangedDefense.value = 0
	
	val movementPenalty = new Stat()
	movementPenalty.value = 0

	val hitPenalty = new Stat()
	hitPenalty.value = 0
	
}