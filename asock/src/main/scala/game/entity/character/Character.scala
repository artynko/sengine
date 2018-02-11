package game.entity.character

import scala.math._
import core.engine.CameraService
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import core.engine.entity.EntityFactory
import core.engine.messages.Clicked
import core.engine.messages.KeyPressed
import core.engine.messages.MoveOrigin
import core.jgml.utils.SVec3
import core.render.Guid
import game.entity.ArmorVest
import game.entity.TileTarget
import game.entity.armor.Armor
import game.entity.messages.AddEntityToSlot
import game.entity.ui.Label
import game.map.generator.Tile
import game.map.generator.TileContainer
import game.user.input.Inventory
import game.user.input.Selection
import game.user.input.TargetTiles
import game.core.turn.TurnManager
import game.core.Constants
import game.entity.HasStats
import game.entity.messages.AddEntityToSlot
import game.core.turn.TurnBased
import game.service.DealDamage
import game.entity.computer.character.MeleeAiCharacter
import game.service.CombatService
import core.jgml.utils.SVec4
import java.awt.event.InputEvent
import game.util.MathHelper._
import game.random.SeededRandom
import game.entity.Soldier
import core.engine.entity.Rendered
import game.entity.ui.IniciativePanel
import game.entity.AssaultRifle
import core.engine.entity.UiElement2D
import core.engine.entity.AlphaIndex
import game.core.Lifecycle
import core.engine.messages.Move
import core.engine.messages.Moved
import core.engine.EventBus
import game.entity.messages.CharacterSelected
import core.render.Border
import game.entity.ui.LogWindow
import game.entity.ui.MouseOverPopup
import scala.collection.mutable.ListBuffer
import game.entity.skill.Skill
import game.entity.InventoryItem
import game.entity.skill.SkillUsed
import game.entity.weapon.Weapon
import game.entity.messages.EntityRemovedFromSlot
import game.entity.messages.RemoveEntityFromSlot
import game.entity.messages.RemoveEntityFromSlot
import game.entity.messages.ItemUnequiped
import core.engine.SceneService
import game.scene.TacticalMissionScene
import game.entity.GunFireEffect

case class MoveToTile(tile: Tile)

object Character {
  def apply() = EntityFactory.create[Character]
}

class Character extends DynamicEntity with HasStats with TurnBased with Portrait {
  val cameraService = inject[CameraService]
  val inventory = inject[Inventory]
  val tileContainer = injectActor[TileContainer]
  val turnManager = injectActor[TurnManager]
  val combatService = inject[CombatService]
  val eventBus = injectActor[EventBus]
  val logWindow = injectActor[LogWindow]
  val mouseOverPopup = injectActor[MouseOverPopup]
  val log = injectActor[LogWindow]
  val sceneService = injectActor[SceneService]

  var armor: Armor = _
  var tile: Tile = _
  var shotPrimary = true
  var shootEffectDuration: Long = 0
  var myTurn = false
  val activeSkills = ListBuffer[Skill]()

  val damageText = EntityFactory.createStatic[Label]
  damageText.size = 16
  damageText.x = 0
  damageText.y = 0
  damageText.hide
  damageText.color = SVec4(1, 1, 1, 1) // white
  val hudText = EntityFactory.createStatic[Label]
  hudText.color = SVec4(1, 1, 1, 1) // white
  hudText.size = 22
  hudText.x = 50
  hudText.y = 50
  hudText.hide

  def nextFrame(elapsedMs: Long) = {
    //cameraService.rotation += 0.05f
    if (Selection.selected.isDefined && Selection.selected.get == this) {
      val p1 = cameraService.posOnScreenFromWorldPos(SVec3(x, y, z))
      val p2 = cameraService.posOnScreenFromWorldPos(SVec3(x, y + 1, z))
      val s1 = p2.y - p1.y
      //logWindow.add(s"length at character: $s1")
    }

    // resizes the skills ui each frame
    if (myTurn) {
      activeSkills zip (0 until activeSkills.size) foreach {
        // position to the middle of the screen
        case (skill, n) => skill.icon.move(((Lifecycle.screenWidth / 2) - (activeSkills.size * (Skill.ICON_SIZE + 10)) / 2) + n * (Skill.ICON_SIZE + 10), 100, -10)
      }
    }

    if (shootEffectDuration > 0) {
      if (SeededRandom.r.nextInt(5) > 3)
        armor.slot(Soldier.SLOT_GUN_EFFECT)._1 match {
          case e: Rendered => e.toogleRendered
          case _ =>
        }
      shootEffectDuration -= elapsedMs
    } else {
      if (armor.slot.contains(Soldier.SLOT_GUN_EFFECT))
        armor.slot(Soldier.SLOT_GUN_EFFECT)._1 match {
          case e: Rendered if e.rendered => e.hide
          case _ =>
        }
    }
  }

  def handleMessage = {
    case SkillUsed(_, e) if e == this => endMyTurn(true)
    case Moved((e, _, x, y)) if e == armor => armor.borderShow
    case Moved((e: MeleeAiCharacter, _, x, y)) if myTurn && alive => mouseOverPopup.setTargets(this, e)
    case Moved((e, m, x, y)) =>
      if (myTurn && alive)
        mouseOverPopup.clearTarget
      armor.borderHide
    case 'myStartTurn => iStartTurn
    case 'myEndTurn => activeSkills foreach (_.myTurnEnd)
    case DealDamage(d) =>
      current.hp.value -= d
      log.add(s"player is hit for $d")
      if (current.hp.value < 1) { // I am dead!
        turnManager.players -= this // remove myself since I am DEAD!
        armor.hide
        tile.contains = None
        endMyTurn(false) // do not notify the turn manager just cleanup stuff
        log.add(s"player dies")
      }
    case EntityRemovedFromSlot(slotId, ch, e: InventoryItem) if ch == this =>
      println("removed from slot" + e)
      e match { // notification from armor 
        case w: Weapon if slotId == 0 => // remove the skills from active skills
          val filteredSkills = activeSkills filter (skill => !w.equipedSkills.contains(skill))
          activeSkills.clear
          activeSkills ++= filteredSkills
        case w: Weapon =>
          val filteredSkills = activeSkills filter (skill => !w.notEquipedSkills.contains(skill))
          activeSkills.clear
          activeSkills ++= filteredSkills
        case entity: InventoryItem =>
          val filteredSkills = activeSkills filter (skill => !entity.skills.contains(skill))
          activeSkills.clear
          activeSkills ++= filteredSkills
        case _ =>
      }
      // notify the rest of the world that something was unequiped
      eventBus.send(ItemUnequiped(this, slotId, e))
    case AddEntityToSlot(slotId, entity) =>
      // check if the entity has any skills assigned to it and if yes add that to active skills
      entity match {
        case w: Weapon if slotId == 0 => activeSkills ++= w.equipedSkills // slot 0 is hands
        case w: Weapon => activeSkills ++= w.notEquipedSkills
        case entity: InventoryItem => activeSkills ++= entity.skills
        case _ =>
      }
      armor.addToSlot(slotId, entity)
    case RemoveEntityFromSlot(e) => armor.removeFromSlot(e)
    case MoveToTile(t) => moveModelToTile(t)
    case msg if Selection.selected.isDefined && Selection.selected.get == this => // I am selected
      msg match {
        case Clicked(Some(entity), modifiers, _) =>
          (entity, modifiers) match {
            case (e: TileTarget, _) => moveCharacterToTile(e)
            case (e: MeleeAiCharacter, button) if (button & InputEvent.BUTTON1_MASK) != 0 && !shotPrimary => // left click
              cleanTargetSelection
              Selection.playerTarget = Some(e)
              e.borderShow
              armor.lookAt(SVec3(x, y, z), SVec3(e.x, e.y, e.z), SVec3(0, 1, 0))
              val currentRange = combatService.getDistance(this, e)
              val weaponRange = current.range.value
              damageText.text = "Hit: " + combatService.getDistance(this, e) + s"% ($currentRange/$weaponRange), Damage: " + (-combatService.computeRangedDmg(this, e)) + ", HP: " + e.current.hp.value
              damageText.move(300, 300, 0)
              damageText.show
            case (e: MeleeAiCharacter, button) if (button & InputEvent.BUTTON3_MASK) != 0 && !shotPrimary => // right click  
              cleanTargetSelection
              armor.lookAt(SVec3(x, y, z), SVec3(e.x, e.y, e.z), SVec3(0, 1, 0))
              combatService.rangedHit(this, e) // attack whatever I clicked
              // show animation
              shootEffectDuration = 500
              cleanTargetTiles // I can't move anymore
              shotPrimary = true // I can't attack with primary weapon anymore
            //Selection.bar.hide
            case _ =>

          }
        case KeyPressed(keys) =>
          keys foreach {
            case "TAB" =>
              if (!inventory.visible) inventory.show(this) else inventory.hide
            case " " =>
              println("turn end")
              endMyTurn(true)
            case _ =>
          }
        case _ =>
      }
    case Clicked(Some(e), _, _) if e == armor => // clicked but I am not selected
      eventBus send CharacterSelected(this)
    //if (sceneService.currentScene == TacticalMissionScene)
    //clickedMe(e) // removed it messes up the ui
    case msg =>
  }

  def equipArmor(a: Armor) = {
    val oldArmor: Armor = if (armor != null) armor else null
    if (oldArmor != null)
      a.transformationMatrix = oldArmor.transformationMatrix
    armor = a
    a.character = this
    registerModifier(armor)
    if (oldArmor != null) {
      oldArmor.unequipAllItems
      unregisterModifier(oldArmor)
      oldArmor.hide
    }
    addToSlot(Soldier.SLOT_GUN_EFFECT, new GunFireEffect)
  }
  def removeFromSlot(e: Entity) = self ! RemoveEntityFromSlot(e)
  def addToSlot(slotId: Int, e: Entity) = self ! AddEntityToSlot(slotId, e)

  def updateHud() = {
    hudText.text = "Iniciative: " + current.iniciative.value + ", Movement: " + current.movement.value + ", HP: " + current.hp.value
    hudText.show
  }

  /**
   * Notify tells if the turn manager should be notified
   */
  def endMyTurn(notify: Boolean) = {
    cleanTargetSelection
    myTurn = false
    current.movement.value = 0
    hudText.hide
    damageText.hide
    Selection.bar.hide
    cleanTargetTiles
    // hide all skill icons
    activeSkills foreach { s =>
      s.icon.hide
      s.myTurnEnd
    }
    if (notify)
      turnManager.myTurnDone
  }

  def myTurnStart() = self ! 'myStartTurn
  def myTurnEnd() = self ! 'myEndTurn

  /**
   * A new turn started for all the characters
   */
  def newTurn() = {
    activeSkills foreach (_.newTurn)
    current.iniciative.value = current.iniciative.value + max.iniciative.value match {
      case i if i > max.iniciative.value * 2 => max.iniciative.value * 2
      case i => i
    }
  }
  /**
   * It is turn for this character, it can take its actions
   */
  def iStartTurn() = {
    // remove iniciative that was needed to start this turn
    current.iniciative.value - Constants.INITIATIVE_FOR_TURN match {
      case i if i < 0 || !alive => endMyTurn(true) // I am dead so just tell the turnManager I am done (this can happend due to the asynchronous dmg dealt
      case i =>
        current.iniciative.value = i
        current.movement.value = max.movement.value
        shotPrimary = false
        clickedMe(armor)
        // show the skill icons
        activeSkills zip (0 until activeSkills.size) foreach {
          case (skill, n) => skill.icon.show
        }
        myTurn = true
        updateCurrentCombatStats
        updateHud
    }
  }

  /**
   * Moves a character to a tile - this is the logical part of the move after click on a tile
   */
  private def moveCharacterToTile(e: game.entity.TileTarget): Unit = {
    current.movement.value = BigDecimal(e.remainingMovement).setScale(2, BigDecimal.RoundingMode.DOWN).toInt
    armor.lookAt(SVec3(x, y, z), SVec3(e.x, e.y, e.z), SVec3(0, 1, 0))
    moveModelToTile(e.target)
    Selection.selected = None
    Selection.effect.hide
    TargetTiles.list foreach (_.hide)
    updateHud
    clickedMe(e)
  }

  def moveToTile(tile: Tile) = self ! MoveToTile(tile)

  override def onMove() = {
    armor.move(x, y, z)
    hudText.text = x + ", " + z
  }

  /**
   * Tries to place a tile
   * returns remaining list of tiles
   */
  def placeTile(tx: Int, ty: Int, distanceToTile: Double, movesRemaining: Double, map: collection.mutable.Map[(Int, Int), Double], isFirst: Boolean): Unit = {
    tileContainer.isOccupied(tx, ty) match {
      case Some(_) if isFirst =>
        val tile = tileContainer.at(tx, ty)
        for (ntx <- -1 to 1; nty <- -1 to 1 if !(ntx == 0 && nty == 0)) {
          val xdist = abs(ntx)
          val ydist = abs(nty)
          val ndtt = sqrt(xdist * xdist + ydist * ydist)
          if (movesRemaining - distanceToTile >= ndtt)
            placeTile(tx + ntx, ty + nty, ndtt, movesRemaining - distanceToTile, map, false)
        }
      case None =>
        if (!map.contains((tx, ty))) // haven't tried this one yet
          map((tx, ty)) = 0
        map((tx, ty)) match { // update the moves remaining if this was more effective route
          case oldRemaining if oldRemaining < movesRemaining - distanceToTile =>
            map((tx, ty)) = movesRemaining - distanceToTile
            // and continue going this route 
            val tile = tileContainer.at(tx, ty)
            for (ntx <- -1 to 1; nty <- -1 to 1 if !(ntx == 0 && nty == 0)) {
              val xdist = abs(ntx)
              val ydist = abs(nty)
              val ndtt = sqrt(xdist * xdist + ydist * ydist)
              if (movesRemaining - distanceToTile >= ndtt)
                placeTile(tx + ntx, ty + nty, ndtt, movesRemaining - distanceToTile, map, false)
            }
          case _ =>
        }
      case _ =>
    }
  }

  def moveTargetTiles() = {
    cleanTargetTiles
    val m = collection.mutable.Map[(Int, Int), Double]()

    placeTile(tile.x, tile.y, 0, current.movement.value, m, true)

    // places target tiles to the keys on the map
    m.foldLeft(TargetTiles.list) {
      case (tiles, ((tx, ty), d)) =>
        val targetTile = tiles.head
        val tile = tileContainer.at(tx, ty)
        tile.contains = Some(targetTile) // the tile now contains the tile target
        targetTile.move(tile.model.x, -1.99f, tile.model.z)
        targetTile.target = tile
        targetTile.show
        targetTile.remainingMovement = d
        tiles.tail
    }
  }

  /**
   * I was clicked, show the movement destinations and bars
   */
  private def clickedMe(e: core.engine.entity.Entity): Unit = {
    cameraService.cameraPan.self ! MoveOrigin(e.x, e.z)
    println("character clicked " + armor.guid)
    Selection.selected = Some(this)
    Selection.moveEffect(x, y, z)
    Selection.showEffect
    inventory.hide
    moveTargetTiles()
  }

  /**
   * Moves self to tile - this is the physical move of the model
   */
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

    t.contains = Some(this)
  }

  private def cleanTargetTiles: Unit = {
    TargetTiles.list foreach { t =>
      t.hide
      if (t.target != null)
        t.target.contains match {
          case Some(tt) if tt.isInstanceOf[TileTarget] =>
            t.target.contains = None
          case _ =>
        }
    }
  }

  private def cleanTargetSelection: Unit = {
    Selection.playerTarget match {
      case Some(old: Border) => old.borderHide;
      case _ =>
    }
    Selection.playerTarget = None
  }
}