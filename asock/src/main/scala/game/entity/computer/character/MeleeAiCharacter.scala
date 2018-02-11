package game.entity.computer.character

import scala.math._
import akka.actor.actorRef2Scala
import core.engine.PathFindingService
import core.engine.entity.DynamicEntity
import core.render.Mesh
import game.core.Constants
import game.core.turn.TurnBased
import game.core.turn.TurnManager
import game.entity.HasStats
import game.entity.character.Character
import game.entity.character.MoveToTile
import game.entity.character.Stat
import game.map.generator.Tile
import game.map.generator.TileContainer
import game.service.CombatService
import core.engine.entity.Clickable
import game.service.DealDamage
import game.util.MathHelper
import game.entity.character.Portrait
import core.render.Textured
import core.render.Border
import core.engine.messages.Moved
import game.user.input.Selection
import core.engine.entity.AlphaIndex
import game.entity.ui.LogWindow
import core.render.Tinted
import core.jgml.utils.SVec4

class MeleeAiCharacter extends DynamicEntity with HasStats with TurnBased with Mesh with Clickable with Portrait with Textured with Border with AlphaIndex with Tinted {
  val meshName = "meele_monster"
  val alphaIndex = 15
  val textureName = "textures/crawler_body.png"
  val pf = inject[PathFindingService]
  val tileContainer = inject[TileContainer]
  val turnManager = inject[TurnManager]
  val combatService = inject[CombatService]
  val log = injectActor[LogWindow]

  var tile: Tile = null
  var target: Option[Character] = None
  base.iniciative.value = 5
  base.movement.value = 10
  base.meeleDmg.value = 7
  base.meeleDefense.value = 2
  base.rangedDefense.value = 0
  base.hp.value = 10
  current.hp.value = 10
  
  borderSize = 3
  borderColor(0.7f, 0, 0)

  def nextFrame(elapsedMs: Long) = {
  }

  def handleMessage = {
    case Moved((e, m, x, y)) if e == this => borderShow
    case Moved((e, m, x, y)) if Selection.playerTarget.isDefined && Selection.playerTarget.get == this => 
    case Moved((e, m, x, y)) => borderHide
    case DealDamage(d) =>
      println("damaz" + d)
      current.hp.value -= d
      if (current.hp.value < 1) { // I am dead!
        destroy
        turnManager.enemies -= this // remove myself since I am DEAD!
        tile.contains = None
        log.add("enemy dies")
      }
    case MoveToTile(t) => moveModelToTile(t)
    case 'myTurnStart =>
      updateCurrentCombatStats
      println("ai my turn star")
      current.iniciative.value - Constants.INITIATIVE_FOR_TURN match {
        case i if i < 0 => turnManager.myTurnDone
        case i =>
          current.iniciative.value = i
          current.movement.value = max.movement.value
          target match {
            case Some(t) => // I have a target from previous turn
              // check if he is in meele range
              (t.tile.x, t.tile.y) match {
                case (x, y) if abs(x - tile.x) <= 1 && abs(y - tile.y) <= 1 && t.alive => // I am in meele range and the target is alive
                case _ => acquireTargetAndMove
              }
            case None => acquireTargetAndMove // I have no target yet, get one and move to him
          }
          target match {
            case Some(t) if abs(t.tile.x - tile.x) <= 1 && abs(t.tile.y - tile.y) <= 1 => // I am in meele range attack
              combatService.meeleHit(MeleeAiCharacter.this, t)
            case _ =>
          }

          // TODO: update movement for the ai, (no need now since it always tries to move to maximum lenght)
          println("ai turn done")
          turnManager.myTurnDone
      }
    // find my target
    case _ =>
  }

  private def acquireTargetAndMove() = {
    val target = getClosestCharacter()
    if (target != null) {
      MeleeAiCharacter.this.target = Some(target)
      // compute the path to him
      val blockedClosure = (x: Int, y: Int) => (x, y) match {
        case (x, y) if (x == tile.x && y == tile.y) || (x == target.tile.x && y == target.tile.y) => false // I don't want mine and targets tile to be treated as occupied
        case (x, y) => tileContainer.isOccupied(x, y) match {
          case None => false
          case Some(_) => true
        }
      }
      val route = pf.pathfindA((tile.x, tile.y), (target.tile.x, target.tile.y), blockedClosure)
      // figure how far can I get with my movement
      val dstTile = route.foldLeft(tileContainer.at(route(0)._1, route(0)._2), 0.0) {
        case ((lastTile, movement), (nx, ny)) =>
          val newTile = tileContainer.at(nx, ny)
          val d = MathHelper.dist(lastTile.x, lastTile.y, newTile.x, newTile.y)
          if ((movement + d > current.movement.value) || newTile == target.tile) // I can't move to this tile anymore
            (lastTile, movement + d)
          else
            (newTile, movement + d)
      }
      moveModelToTile(dstTile._1)
      lookAt(target)
    } else {
      MeleeAiCharacter.this.target = None
    }
  }

  def getClosestCharacter(): Character = {
    val r = turnManager.players.foldLeft[(Float, Character)]((0.0f, null)) {
      case ((d, null), c: Character) if checkPathingObscured(c) =>
        val nd = MathHelper.dist(c.x, c.z, x, z)
        (nd.toFloat, c) // the first one just store distance to him
      case ((d, null), c: Character) => ((d, null))
      case ((d, current: Character), c: Character) if MathHelper.dist(c.x, c.z, x, z) < d && checkPathingObscured(c) => (MathHelper.dist(c.x, c.z, x, z).toFloat, c)
      case ((d, current: Character), c: Character) => (d, current)
    }
    r._2
  }

  /**
   * Checks if this character isn't blocked from all sides
   */
  def checkPathingObscured(c: Character) = {
    val ctile = c.tile
    // I check that there is atleast one open tile next to this character
    val r = for (ntx <- ctile.x - 1 to ctile.x + 1; nty <- ctile.y - 1 to ctile.y + 1 if !tileContainer.isOccupied(ntx, nty).isDefined) yield true
    r.length > 0
  }

  def moveToTile(t: Tile) = self ! MoveToTile(t)

  def myTurnStart() = self ! 'myTurnStart
  def myTurnEnd() = {}

  def newTurn() = {
    current.iniciative.value = current.iniciative.value + max.iniciative.value match {
      case i if i > max.iniciative.value * 2 => max.iniciative.value * 2
      case i => i
    }
  }

  private def moveModelToTile(t: game.map.generator.Tile): Unit = {
    if (tile != null)
      tile.contains = None
    tile = t
    try {
      internalMove(t.model.x, t.model.y, t.model.z)
    } catch {
      case e: NullPointerException =>
        println(t.model)
        throw e
      case e: Throwable => throw e
    }
    t.contains = Some(MeleeAiCharacter.this)
  }



}