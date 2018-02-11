package game.core.turn

import core.engine.entity.DynamicEntity
import core.app.RegisteringComponent
import game.core.Constants
import core.engine.entity.EntityFactory
import game.entity.ui.Label
import game.entity.HasStats
import game.entity.character.Character
import game.entity.computer.character.MeleeAiCharacter
import game.entity.ui.IniciativePanel
import core.engine.entity.Entity
import scala.collection.mutable.ListBuffer
import core.engine.EngineCore
import core.engine.SceneService
import core.render.RenderingCore
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global
import core.engine.EventBus
import game.entity.messages.LoadScene
import game.scene.SquadEquipScene
import game.scene.Loading
import game.scene.SquadEquipScene
import core.jgml.utils.SVec4
import game.scene.TacticalMissionLoader
import game.scene.TacticalMissionLoader
import game.scene.TacticalMissionScene

class TurnManager extends DynamicEntity with RegisteringComponent {
  val players = collection.mutable.ListBuffer[Character]()
  val enemies = collection.mutable.ListBuffer[MeleeAiCharacter]()
  val iniciativePanel = injectActor[IniciativePanel]
  val eventBus = inject[EventBus]
  val sceneService = injectActor[SceneService]
  val r = inject[RenderingCore]

  var turn = 0
  var playersTurn: Option[Boolean] = None // None = it is the start of a new turn you should pick whoever has the biggest initiative
  val hudText = EntityFactory.createStatic[Label]
  hudText.text = "Turn #" + turn
  hudText.size = 26
  hudText.x = 50
  hudText.y = 80
  hudText.show

  def nextFrame(elapsedMs: Long) = {
  }

  def handleMessage = {
    case 'nextTurn =>
      turn += 1
      hudText.text = "Turn #" + turn
      playersTurn = None
      players foreach (_.newTurn)
      enemies foreach (_.newTurn)
      handleNextCharacter()
    case 'charactersTurnDone =>
      if (enemies.size == 0) {
        // unload the scene
        r.updateBackgroundColor(SVec4(0, 0, 0, 1))
        sceneService.loadScene(Loading)
        sceneService.setScene(TacticalMissionScene)
        sceneService.switchScene(SquadEquipScene, () => {
          sceneService.unsetScene
          sceneService.unloadScene(TacticalMissionScene) // remove current tactical
          sceneService.deleteScene(TacticalMissionScene) // remove current tactical
          sceneService.unloadScene('characters)
          Unit
        })
      } else {
        handleNextCharacter
      }
    case _ =>
  }

  def myTurnDone() = self ! 'charactersTurnDone

  /**
   * Returns a list of turnbased entitities in the order thay should take they turns, length is the length of the list
   */
  def sortByInitiativeAndFigureOrder(currentPlayers: List[(Entity with TurnBased, Int)], currentEnemies: List[(Entity with TurnBased, Int)], length: Int, pt: Option[Boolean]): List[Entity with TurnBased] = {
    val p = currentPlayers.filter(_._2 >= Constants.INITIATIVE_FOR_TURN).sortWith { // only leave players that have iniciative and then sort them 
      case (c1, c2) => c1._2 > c2._2
    }
    val e = currentEnemies.filter(_._2 >= Constants.INITIATIVE_FOR_TURN).sortWith { // only leave enemies that have iniciative and then sort them 
      case (c1, c2) => c1._2 > c2._2
    }
    pt match {
      case None => // brand new turn figure out who starts
        if (p(0)._2 > e(0)._2) {
          playersTurn = Some(true)
          getNextCharacter(p, e, length)
        } else {
          playersTurn = Some(false)
          getNextCharacter(e, p, length)
        }
      case Some(true) => // this turn is for pcs
        getNextCharacter(p, e, length)
      case Some(false) => // this turn is for npcs
        getNextCharacter(e, p, length)
    }
  }

  /**
   * Gets the next charactert from currentCharacters, updates his initiative and then follows with nextCharacters list, this ensures that players and enemies alternate
   */
  def getNextCharacter(currentCharacters: List[(Entity with TurnBased, Int)], nextCharacters: List[(Entity with TurnBased, Int)], length: Int): List[Entity with TurnBased] = {
    if (length == 0) // stop iteration
      return Nil

    val sortedCurrent = currentCharacters.filter(_._2 >= Constants.INITIATIVE_FOR_TURN).sortWith { // only leave players that have iniciative and then sort them 
      case (c1, c2) => c1._2 >= c2._2
    }
    if (sortedCurrent.size == 0) // just get the one from next if we are already 0
      return getNextCharacter(nextCharacters, sortedCurrent, length - 1)

    val nextCharacter = sortedCurrent(0)._1

    // remove the initiative from the first in current because it will be his turn at this point
    val updatedCurrentCharacters = sortedCurrent zip (0 until sortedCurrent.size) map {
      case ((entity, currentIniciative), 0) => (entity, currentIniciative - Constants.INITIATIVE_FOR_TURN)
      case ((entity, currentIniciative), _) => (entity, currentIniciative)
    }

    nextCharacter :: getNextCharacter(nextCharacters, updatedCurrentCharacters, length - 1)
  }

  def handleNextCharacter() = {
    val p = players map (p => (p.asInstanceOf[Entity with TurnBased], p.current.iniciative.value))
    val e = enemies map (e => (e.asInstanceOf[Entity with TurnBased], e.current.iniciative.value))

    // merge them into one list
    val sorted = sortByInitiativeAndFigureOrder(p toList, e toList, 10, playersTurn)
    for (e <- playersTurn)
      playersTurn = Some(!e)
    //println(sorted.size + " :" + sorted)
    sorted match {
      case Nil => nextTurn
      case head :: _ =>
        iniciativePanel.updatePanel(sorted.tail) // I don't want the one that is currently selected to be shown in the panel
        head.myTurnStart
    }
  }

  def nextTurn() = self ! 'nextTurn

}
