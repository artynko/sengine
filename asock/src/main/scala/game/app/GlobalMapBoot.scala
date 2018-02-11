package game.app

import scala.concurrent
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import core.app.ApplicationContext
import core.app.Component
import core.engine.CameraPan
import core.engine.CameraService
import core.engine.EventBus
import core.engine.SceneService
import core.engine.entity.AlphaIndex
import core.engine.entity.Entity
import core.engine.entity.EntityFactory
import core.engine.entity.UiElement2D
import core.engine.entity.primitive.PrimitiveBox2D
import core.jgml.utils.SVec4
import core.render.RenderingCore
import game.core.turn.TurnManager
import game.core.turn.TurnManager
import game.core.turn.TurnManager
import game.entity.AssaultRifle
import game.entity.Backpack
import game.entity.Bar
import game.entity.CombatTurtleneck
import game.entity.GrassTileModel
import game.entity.Grenade
import game.entity.GunFireEffect
import game.entity.GunFireEffect
import game.entity.HeavyMg
import game.entity.Pistol
import game.entity.SelectionEfect
import game.entity.Shotgun
import game.entity.TileTarget
import game.entity.TilesBlock
import game.entity.TilesBlock
import game.entity.TilesBlockFlat
import game.entity.TilesBlockGrass
import game.entity.Tree1
import game.entity.character.Character
import game.entity.computer.character.MeleeAiCharacter
import game.entity.ui.IniciativePanel
import game.entity.ui.IniciativePortrait
import game.entity.ui.InventoryDragAndDrop
import game.entity.ui.Label
import game.entity.ui.Rectangle
import game.entity.ui.StatsPanel
import game.entity.ui.inventory.ItemSlot
import game.map.generator.TileContainer
import game.repository.CharacterRepository
import game.scene._
import game.screen.SquadEquipScreen
import game.entity.messages.LoadScene
import game.repository.EnemyRepository
import game.entity.Soldier
import game.entity.ui.LogWindow
import game.user.input.Inventory
import core.engine.EngineCore
import core.render.Textured
import core.engine.entity.primitive.PrimitiveGrid
import core.engine.entity.StaticEntity
import com.hackoeur.jglm.Vec4
import core.render.NonCulled
import core.render.VertexColorShader
import scala.util.Random
import game.map.generator.Tile
import game.map.generator.GlobalTileType
import game.map.generator.PossibleTile
import core.engine.Point
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import core.engine.entity.Rendered

class GlobalMap(val colors: Array[Vec4], w: Int) extends PrimitiveGrid(colors, w) with StaticEntity with Textured with VertexColorShader with Rendered with NonCulled {
  val textureName = "textures/assault_rifle.png"
}
case class Mountain() extends GlobalTileType
case class Forrest() extends GlobalTileType
case class Mud() extends GlobalTileType

object GlobalMap extends App with Component {
  val ctx = new ApplicationContext()
  val renderingCore = inject[RenderingCore]
  val sceneService = injectActor[SceneService]
  val eventBus = injectActor[EventBus]
  val cameraService = inject[CameraService]

  val m = new AssaultRifle(false)
  m.hide
  cameraService.center(0, 0)
  cameraService.distance = -1.5f
  cameraService.xRotation = 0.00f

  // conf
  val objectRadius = 40
  val probFactor = 200

  // create the grid
  val map = new Array[Array[PossibleTile]](108)

  for (y <- 0 until 108) {
    map(y) = new Array[PossibleTile](192)
    for (x <- 0 until 192) {
      val ptile = new PossibleTile()
      ptile.tiles ++= List(Forrest(),Forrest(),Forrest(), Mountain(), Mud(), Mud())
      map(y)(x) = ptile
    }
  }
  /**
   * 
   * Pick first (Town, Farm, Industrial, Mine), create surrounding cells add Nothing type for those too, based on some kind of spacing, remove change for whatever was picked
   * resolve next cell, each cell contains how far is it to the various large objects, 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   */

  val r = new Random
  for (y <- 0 until 108) {
    for (x <- 0 until 192) {
      val current = map(y)(x)
      if (current.resolvedType == null)
        current.resolve(current.tiles(r.nextInt(current.tiles.size)))
      if (current.center == None) {
        current.center = Some(Point(y, x))
        val xsize = (x + r.nextInt(objectRadius) + 3) 
        for (ny <- y until (y + r.nextInt(objectRadius) + 3) if ny < 108) {
          for (nx <- x until xsize if nx < 192) {
            val tile = map(ny)(nx)
            tile.center match {
              case None =>
                tile.tiles.clear
                tile.tiles ++= (0 until probFactor) map (_ => current.resolvedType)
                tile.center = Some(Point(y, x))
              case Some(_) =>
            }
          }
        }
      }
    }
  }
  /*
  for (y <- 0 until 50) {
    for (x <- 0 until 50) {
    	map(y)(x).resolve(Forrest())
    }
  }
  */
  val l = map flatMap (m => m.map(t => t.resolvedType))
  val colors = l map {
    _ match {
      case Forrest() => SVec4(0, 0.6f, 0, 1) // I resolved forrest
      case Mud() => SVec4(0.6f, 0.6f, 0, 1)
      case Mountain() => SVec4(0.5f, 0.5f, 0.5f, 1f)
      case _ => SVec4(1f, 0, 0f, 1f)
    }
  }

  val c = new GlobalMap(colors,
    192)
  c.move(-0.96f, -0.54f, 0)
  c.scale = 0.01f
  
  val mm = new Array[Array[Vec4]](2)
  mm(0) = new Array[Vec4](3)
  mm(0)(0) = SVec4(1, 0, 0, 1)
  mm(0)(1) = SVec4(1, 0, 0, 1)
  mm(0)(2) = SVec4(0, 1, 0, 1)
  mm(1) = new Array[Vec4](3)
  mm(1)(0) = SVec4(0, 1, 0, 1)
  mm(1)(1) = SVec4(0, 0, 1, 1)
  mm(1)(2) = SVec4(0, 0, 1, 1)
  
  /*val a = new GlobalMap(list toList,
    192)
  a.move(-0.96f, -0.54f, 0)
  a.scale = 0.01f
  val b = new GlobalMap(list toList,
    192)
  b.move(-0.96f, -0.54f, 0)
  b.scale = 0.01f*/

  renderingCore.start
  renderingCore.showFpsCounter
  renderingCore.swapInterval = 1
}
