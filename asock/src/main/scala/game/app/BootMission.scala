package game.app

import game.map.generator.TileContainer
import game.user.input.TargetTiles
import game.core.turn.TurnManager
import game.entity.TilesBlockGrass
import game.map.generator.Tile
import game.entity.ui.IniciativePanel
import core.engine.entity.EntityFactory
import game.entity.TilesBlockFlat
import game.entity.Tree1
import game.entity.Bar
import game.entity.computer.character.MeleeAiCharacter
import game.entity.TilesBlock
import game.random.SeededRandom
import game.entity.Grass
import game.user.input.Selection
import core.engine.entity.Entity
import game.entity.ArmorVest
import game.entity.CombatTurtleneck
import game.entity.TileTarget
import game.entity.Soldier
import game.entity.SelectionEfect
import game.entity.GrassTileModel
import game.entity.Grenade
import game.entity.HeavyMg
import game.entity.Backpack
import game.entity.AssaultRifle
import game.entity.Pistol
import game.entity.GunFireEffect
import core.app.ApplicationContext
import core.engine.CameraService
import core.app.Component
import core.render.RenderingCore
import core.jgml.utils.SVec4
import game.core.Lifecycle
import game.entity.TilesBlockDarkGrass
import game.entity.ui.InventoryDragAndDrop
import game.entity.ui.inventory.InventorySlot
import game.entity.Shotgun
import game.entity.ui.SkillIcon
import game.entity.TargetingSphere
import game.repository.EnemyRepository
import core.engine.EngineCore
import core.engine.SceneService
import game.scene.TacticalMissionScene

object BootMission extends App with Component {
  val ctx = new ApplicationContext()
  val renderingCore = inject[RenderingCore]
  val sceneService = injectActor[SceneService]
  val enemyRepository = inject[EnemyRepository]
  renderingCore.swapInterval = 0
  
  renderingCore.showFpsCounter
  renderingCore.updateBackgroundColor(SVec4(0, 0.6f, 0.6f, 1))
  val iniciativePanel = EntityFactory.create[IniciativePanel]
  val turnManager = injectActor[TurnManager]
  EntityFactory.create[TileContainer]
  val tileContainer = ctx.inject[TileContainer]

  val s1 = EntityFactory.create[game.entity.character.Character]
  s1.assignPortrait(0, 0)
  s1.equipArmor(new CombatTurtleneck)
  s1.addToSlot(ArmorVest.SLOT_GUN, new AssaultRifle(true))
  s1.addToSlot(Soldier.SLOT_GUN_EFFECT, new GunFireEffect)
  s1.addToSlot(ArmorVest.SLOT_HIP, new Pistol)
  val s2 = EntityFactory.create[game.entity.character.Character]
  s2.assignPortrait(1, 0)
  s2.equipArmor(new ArmorVest)
  s2.addToSlot(ArmorVest.SLOT_GUN, new HeavyMg(true))
  s2.addToSlot(ArmorVest.SLOT_HIP, new Grenade(true))
  s2.addToSlot(ArmorVest.SLOT_BACK, new Backpack)
  s2.addToSlot(Soldier.SLOT_GUN_EFFECT, new GunFireEffect)
  s2.addToSlot(4, new Grenade(true))
  val s3 = EntityFactory.create[game.entity.character.Character]
  s3.assignPortrait(1, 0)
  s3.equipArmor(new ArmorVest)
  s3.addToSlot(ArmorVest.SLOT_GUN, new Shotgun(true))
  s3.addToSlot(ArmorVest.SLOT_HIP, new Pistol)
  s3.addToSlot(ArmorVest.SLOT_BACK, new HeavyMg(true))
  s3.addToSlot(Soldier.SLOT_GUN_EFFECT, new GunFireEffect)
  s3.addToSlot(4, new Grenade(true))
  
  s1.current.hp.value = s1.max.hp.value
  s2.current.hp.value = s2.max.hp.value
  s3.current.hp.value = s3.max.hp.value
  
  turnManager.players += s1
  turnManager.players += s2
  turnManager.players += s3

  val e1 = EntityFactory.create[MeleeAiCharacter]
  e1.assignPortrait(0, 1)
  /*val e2 = EntityFactory.create[MeleeAiCharacter]
  e2.assignPortrait(0, 1)
  val e3 = EntityFactory.create[MeleeAiCharacter]
  e3.assignPortrait(0, 1)
  val e4 = EntityFactory.create[MeleeAiCharacter]
  e4.assignPortrait(0, 1)
  val e5 = EntityFactory.create[MeleeAiCharacter]
  e5.assignPortrait(0, 1)*/

  enemyRepository += e1
  /*enemyRepository += e2
  enemyRepository += e3
  enemyRepository += e4
  enemyRepository += e5*/
  
  sceneService.setScene(TacticalMissionScene) 
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
  
  sceneService.unsetScene

  s1.moveToTile(tileContainer.at(19, 10))
  s2.moveToTile(tileContainer.at(19, 11))
  s3.moveToTile(tileContainer.at(18, 10))
  e1.moveToTile(tileContainer.at(42, 42))
/*  e2.moveToTile(tileContainer.at(44, 40))
  e3.moveToTile(tileContainer.at(40, 43))
  e4.moveToTile(tileContainer.at(41, 40))
  e5.moveToTile(tileContainer.at(40, 41)) */

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

  val cameraService = ctx.getInstance(classOf[CameraService])

  turnManager.nextTurn
  renderingCore.start
}