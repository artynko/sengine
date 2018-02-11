package game.scene

import scala.Array.canBuildFrom
import scala.collection.mutable.Stack
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Random
import scala.util.Success

import core.engine.CameraService
import core.engine.Point
import core.engine.SceneService
import core.engine.entity.AlphaIndex
import core.engine.entity.DynamicEntity
import core.jgml.utils.SVec3
import core.jgml.utils.SVec4
import core.render.NoDepthTest
import core.render.texture.InMemoryTexture
import core.render.texture.PngImage
import core.utils.MathUtil
import game.app.CityTile
import game.app.Empty
import game.app.Farm
import game.app.Industrial
import game.app.Unresolved
import game.entity.messages.LoadScene
import game.entity.ui.Rectangle
import game.map.entity.City3Model
import game.map.entity.Farm1Model
import game.map.entity.InMemoryTexturedTile
import game.map.entity.SquadEntity
import game.map.generator.City
import game.map.generator.GeneratorRepository
import game.map.generator.ModelTile

/**
 * Loads the squad equip scene
 */
class GlobalMapSceneLoader extends DynamicEntity {
  val sceneService = injectActor[SceneService]
  val cameraService = inject[CameraService]
  val generatorRepository = new GeneratorRepository()

  /**
   * Invoked wherever a next frame is about to rendered, the entity should do all the stuff it wants at this point
   * @param elapsedMs - miliseconds elapsed since last frame was rendered
   */
  def nextFrame(elapsedMs: Long) = {}
  /**
   * Invoked whenever a message is being sent to this entity
   */
  def handleMessage: PartialFunction[Any, Unit] = {
    case LoadScene(GlobalMapScene, startCmd) =>
      sceneService.unloadScene(sceneService.currentScene)
      sceneService.unsetScene
      cameraService.center(0, 0)
      cameraService.distance = -0.9f
      cameraService.rotation = 0
      cameraService.xRotation = 0.6f

      val squad = SquadEntity()
      squad.move(0, 0, 0)
      squad.scale = 0.007f

      // conf
      val objectRadius = 40
      val probFactor = 200

      val cities = collection.mutable.ArrayBuffer[City]()

      val r = new Random
      val citySpread = 3
      val possibleTiles = List(Farm(1), CityTile(citySpread), Industrial(), Empty(), Empty(), Empty(), Empty(), Empty()) ++ ((0 until 50) map (_ => Empty()))
      // create the grid

      val size = 128
      val citiesDistance = 20

      val map = new Array[Array[ModelTile]](size)
      for (i <- 0 until size) {
        map(i) = new Array[ModelTile](size)
        for (n <- 0 until size)
          if (i < 6 || i > size - 6 || n < 6 || n > size - 6)
            map(i)(n) = new ModelTile(Unresolved(), null, Point(i, n), List(Empty())) // only empty tile on the borders
          else
            map(i)(n) = new ModelTile(Unresolved(), null, Point(i, n), possibleTiles)
      }

      // now start from the middle
      resolveTile(size / 2, size / 2, Stack())

      def handleRandomTile(current: ModelTile): Unit = {
        val x = current.location.x
        val y = current.location.y
        // now figure out 
        val resolvedType = current.possibleTileTypes(r.nextInt(current.possibleTileTypes.size))
        resolvedType match {
          case _ if current.possibleTileTypes.size == 1 => current.tileType = current.possibleTileTypes(0) // there was only one possible outcome so just set that one up (used by citygenerator)
          case CityTile(_) if cities.nonEmpty => // figure out if there is a city close
            val distanceToClosestCity = cities map { city =>
              MathUtil.distance(Point(x, y), city.center.location)
            } min;
            if (distanceToClosestCity < citiesDistance + r.nextInt(citiesDistance * 20)) {
              current.possibleTileTypes = current.possibleTileTypes filter {
                case CityTile(_) => false
                case _ => true
              }
            } else {
              current.tileType = CityTile(citySpread + r.nextInt(citySpread) - 2)
              cities += City(current)
            }
          case Farm(_) => // find the nearest city
            val closestCity = cities.foldLeft((10000, City(null))) {
              case ((closestDist, current), city) =>
                val dist = MathUtil.distance(Point(x, y), city.center.location)
                if (dist < closestDist) (dist, city) else (closestDist, current) // find out what is the closest city
            }
            if (closestCity._1 > 8 && closestCity._1 < 32 && closestCity._2.farms.size < 4) { // only spawn farms in a close distance from a city, and if the city already hasn't have a farm limit fullfilled
              current.tileType = resolvedType
              closestCity._2.farms += current
            }
          case CityTile(_) => // my first city
            current.tileType = resolvedType
            cities += City(current)
          case _ => current.tileType = resolvedType
        }
        if (current.tileType == Unresolved()) // haven't resolved anything try it again
          handleRandomTile(current)
      }

      def resolveTile(x: Int, y: Int, toBeResolved: Stack[ModelTile]): Unit = {
        var tr = toBeResolved
        val current = map(x)(y)
        if (current.tileType == Unresolved()) {
          handleRandomTile(current)

        }
        // resolve tiles around me (for multi tile objects)
        for (nx <- (x - 1) to (x + 1)) {
          for (ny <- (y - 1) to (y + 1) if nx >= 0 && nx < size && ny >= 0 && ny < size && !(nx == x && ny == y)) {
            val next = map(nx)(ny)
            generatorRepository.findGenerator(current.tileType).handleNextTile(current, next)
            next.tileType match {
              case Unresolved() if !tr.contains(next) => tr = tr :+ next // next was still unresolved I add it into the list that is supposed to be resolved
              case _ =>
            }
          }
        }
        tr.isEmpty match {
          case true =>
          case false =>
            val next = tr.head
            resolveTile(next.location.x, next.location.y, tr.tail)
        }

      }

      map(0)(0).tileType = CityTile(1)
      map(size - 1)(0).tileType = Farm(1)

      map foreach { s =>
        println(s.map(_.tileType.toString().substring(0, 1)).mkString(" "))
      }

      val xcolors = map.flatMap { row =>
        val result = row.reverse.map { tile =>
          tile.tileType match {
            case Farm(_) => SVec4(0.90f + r.nextInt(100) / 1000f, 0.90f + r.nextInt(100) / 1000f, 0, 1)
            case CityTile(0) => SVec4(0.6f, 0.6f, 0.6f, 1)
            case CityTile(1) => SVec4(0.5f, 0.5f, 0.5f, 1)
            case CityTile(2) => SVec4(0.45f, 0.45f, 0.45f, 1)
            case CityTile(_) => SVec4(0.4f, 0.4f, 0.4f, 1)
            case Industrial() => SVec4(0, 0.5f, 0.5f, 1)
            case Empty() => SVec4(0, 0.90f + r.nextInt(100) / 1000f, 0, 1)
            // unresolved should not happen
          }
        }
        result
      }
      val density = 16

      val m = new InMemoryTexturedTile(size * density)
      m.scale = 2.0f
      m.move(0f, 0f, 0f)

      val globalTiles = new PngImage("textures/globalTileSet.png")
      // go through the map and copy the tiles from tile set into the texture
      for (x <- 0 until size; y <- 0 until size) {
        map(y)(x).tileType match {
          case Farm(_) => r.nextInt(3) match {
            case 0 => copyPixels(m, x, y, globalTiles, 16, 48, 16)
            case 1 => copyPixels(m, x, y, globalTiles, 16, 64, 16)
            case 2 => copyPixels(m, x, y, globalTiles, 16, 80, 16)
          }
          case CityTile(0) => copyPixels(m, x, y, globalTiles, 0, 48, 16)
          case CityTile(1) => copyPixels(m, x, y, globalTiles, 0, 32, 16)
          case CityTile(2) => copyPixels(m, x, y, globalTiles, 0, 16, 16)
          case CityTile(_) => copyPixels(m, x, y, globalTiles, 0, 0, 16)
          case Industrial() => copyPixels(m, x, y, globalTiles, 32, 0, 16)
          case Empty() => r.nextInt(3) match {
            case 0 => copyPixels(m, x, y, globalTiles, 16, 0, 16)
            case 1 => copyPixels(m, x, y, globalTiles, 16, 16, 16)
            case 2 => copyPixels(m, x, y, globalTiles, 16, 32, 16)
          }
        }
      }

      def copyPixels(dst: InMemoryTexturedTile, x: Int, y: Int, src: PngImage, sx: Int, sy: Int, size: Int) = {
        val s = r.nextInt(4) // randomize the tile orientation
        for (xx <- 0 until size; yy <- 0 until size) {
          val color = s match {
            case 0 => src.pixelColorAt(sx + xx, sy + yy)
            case 1 => src.pixelColorAt(sx + yy, sy + xx)
            case 2 => src.pixelColorAt(sx + size - yy - 1, sy + size - xx - 1)
            case 3 => src.pixelColorAt(sx + size - xx - 1, sy + size - yy - 1)
          }
          dst.texturePointColor(x * size + xx, y * size + yy, color)
        }
      }

      val i = new Rectangle(0, 0, 384, 384) with InMemoryTexture with AlphaIndex with NoDepthTest {
        val alphaIndex = 30
        val textureWidth = size * density
        val textureHeight = size * density
        val textureData = new Array[Float](textureWidth * textureHeight * 3)
      }

      for (n <- 0 until xcolors.length) {
        val c = xcolors(n)
        val x = (n / size) * density
        val y = (n % size) * density
        for (xx <- x until x + density; yy <- y until y + density)
          if (xx - x == 0 || yy - y == 0) {
            i.texturePointColor(xx, yy, SVec3(c.getX - 0.2f, c.getY - 0.2f, c.getZ - 0.2f))
          } else {
            i.texturePointColor(xx, yy, SVec3(c.getX, c.getY, c.getZ))
          }
      }

      // place the city models on the map
      cities foreach {
        case c =>
          println(c.center.location.x.toFloat, c.center.location.y.toFloat)
          val x = (c.center.location.x.toFloat * 2 / size) - 1
          val y = (c.center.location.y.toFloat * 2 / size) - 1
          val a = new City3Model
          a.scale = 1f / size
          a.move(x + 1f / size, 0, -y - 1f / size)
          a.move(a.x * 2f, a.y * 2f, a.z * 2f)
          a.scale = a.scale * 2f
          // place the farms belonging to this city
          c.farms foreach { f =>
            val x = (f.location.x.toFloat * 2 / size) - 1
            val y = (f.location.y.toFloat * 2 / size) - 1
            val a = Farm1Model()
            a.scale = 1f / size
            a.move((x + 1f / size) * 2f, 0, (-y - 1f / size) * 2f)
            a.scale = a.scale * 2f
          }

      }

      startCmd() onComplete {
        case Success(s) =>
        case Failure(f) =>
      }
    case _ =>
  }

}