package game.scene

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import core.engine.CameraService
import core.engine.SceneService
import core.engine.entity.DynamicEntity
import core.engine.entity.Entity
import core.engine.entity.EntityFactory
import game.core.turn.TurnManager
import game.entity.Bar
import game.entity.Grass
import game.entity.GrassTileModel
import game.entity.SelectionEfect
import game.entity.TileTarget
import game.entity.TilesBlock
import game.entity.TilesBlockDarkGrass
import game.entity.TilesBlockFlat
import game.entity.TilesBlockGrass
import game.entity.Tree1
import game.entity.computer.character.MeleeAiCharacter
import game.entity.messages.LoadScene
import game.map.generator.Tile
import game.map.generator.TileContainer
import game.random.SeededRandom
import game.repository.CharacterRepository
import game.repository.EnemyRepository
import game.user.input.Selection
import game.user.input.TargetTiles
import game.entity.Shotgun
import core.render.RenderingCore
import core.jgml.utils.SVec4

class TacticalMissionLoader extends DynamicEntity {
  println("creating tacticalMission loader")
  val sceneService = injectActor[SceneService]
  val cameraService = inject[CameraService]
  val enemyRepository = inject[EnemyRepository]
  val characterRepository = inject[CharacterRepository]
  // this maybe should be part of the scene
  val tileContainer = injectActor[TileContainer]
  val turnManager = injectActor[TurnManager]
  val renderingCore = inject[RenderingCore]

  /**
   * Invoked wherever a next frame is about to rendered, the entity should do all the stuff it wants at this point
   * @param elapsedMs - miliseconds elapsed since last frame was rendered
   */
  def nextFrame(elapsedMs: Long) = {}
  /**
   * Invoked whenever a message is being sent to this entity
   */
  def handleMessage = {
    case LoadScene(TacticalMissionScene, startCmd) =>
      println("event received")
      sceneService.unloadScene(Loading) // should unload squad equip
      sceneService.loadScene('characters)
      cameraService.distance = -27f
      cameraService.xRotation = 0.50f
      renderingCore.updateBackgroundColor(SVec4(0, 0.6f, 0.6f, 1))
      val e1 = EntityFactory.create[MeleeAiCharacter]
      e1.assignPortrait(0, 1)
      val e2 = EntityFactory.create[MeleeAiCharacter]
      e2.assignPortrait(0, 1)
      val e3 = EntityFactory.create[MeleeAiCharacter]
      e3.assignPortrait(0, 1)
      val e4 = EntityFactory.create[MeleeAiCharacter]
      e4.assignPortrait(0, 1)
      val e5 = EntityFactory.create[MeleeAiCharacter]
      e5.assignPortrait(0, 1)
      sceneService.setScene(TacticalMissionScene)
      enemyRepository.list.clear

      enemyRepository += e1
      /*enemyRepository += e2
      enemyRepository += e3
      enemyRepository += e4
      enemyRepository += e5*/
      
      enemyRepository() foreach (turnManager.enemies += _)

      val s = 120
      val trees = collection.mutable.ListBuffer[Entity]()
      val darkGrasses = collection.mutable.ListBuffer[Entity]()
      val grasses = collection.mutable.ListBuffer[Entity]()
      val tt = 0 until s map { x =>
        0 until s map { y =>
          val tile = Tile(x, y, new GrassTileModel)
          if (SeededRandom.r.nextInt(100) > 98) {
            val tree = EntityFactory.createStatic[Tree1]
            tree.rotateY(SeededRandom.r.nextInt().toFloat)
            tree.move(0, 0, -5)
            tile.contains = Some(tree)
            tree.move(tile.model.x, tile.model.y, tile.model.z)
            trees += tree
          }
          if (SeededRandom.r.nextInt(50) > 35) {
            val grass = EntityFactory.createStatic[Grass]
            val a = 100
            val d = 100
            val offset = SeededRandom.r.nextInt(a).toFloat / d
            grass.scale = offset + 0.5f
            grass.move(tile.model.x + offset, tile.model.y, tile.model.z + offset)
            grass.scale = offset * 4
            darkGrasses += grass
          }

          if (SeededRandom.r.nextInt(50) > 11) {
            val grass = EntityFactory.createStatic[Grass]
            val a = 100
            val d = 100
            val offset = (SeededRandom.r.nextInt(a).toFloat / d) + 1
            grass.scale = offset
            grass.move(tile.model.x + offset, tile.model.y, tile.model.z + offset)
            grasses += grass
          }
          tile
        } toList
      } toList

      tileContainer.addTiles(tt)
      Thread.sleep(1000)
      println(tileContainer.isOccupied(0, 0))
      val tbg = new TilesBlockGrass(11)
      val tbdg = new TilesBlockDarkGrass(11)
      val tb = new TilesBlockFlat(0)
      val tbs = EntityFactory.createStatic[TilesBlock]

      tbs.entities = trees toList;
      tb.entities = (tileContainer.modelBlock(0, 0, s) toList);
      tbg.entities = grasses toList;
      tbdg.entities = darkGrasses toList;

      characterRepository() zip (0 until characterRepository().size) foreach {
        case (ch, n) =>
          if (n == 0)
            Selection.selected = Some(ch)
          ch.moveToTile(tileContainer.at(19 + n, 10))
          ch.current.hp.value = ch.max.hp.value
          // ch.addToSlot(0, new Shotgun(true))
          turnManager.players += ch
      }

      e1.moveToTile(tileContainer.at(42, 42))
      e2.moveToTile(tileContainer.at(44, 40))
      e3.moveToTile(tileContainer.at(40, 43))
      e4.moveToTile(tileContainer.at(41, 40))
      e5.moveToTile(tileContainer.at(40, 41))

      TargetTiles.list = 0 until 1000 map { _ =>
        val t = EntityFactory.createStatic[TileTarget]
        t.hide
        t
      } toList

      Selection.effect = new SelectionEfect
      Selection.effect.move(0, -1.97f, 0)
      Selection.effect.hide
      val bar = EntityFactory.create[Bar]
      Selection.bar = bar
      bar.hide
      startCmd() onComplete {
        case Success(s) =>
          turnManager.nextTurn
        case Failure(e) => throw new RuntimeException("switch failed" + e)
      }
    case _ =>
  }

}