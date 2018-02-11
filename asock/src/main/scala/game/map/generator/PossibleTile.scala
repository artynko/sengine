package game.map.generator

import core.engine.Point

/**
 * Used by the global generator
 */
class PossibleTile {
  val tiles = collection.mutable.ArrayBuffer[GlobalTileType]()
  /**
   * A center of the "object" this tile belongs to
   */
  var center: Option[Point] = None
  var resolvedType: GlobalTileType = null 
  
  def resolve(tile: GlobalTileType) = resolvedType = tile
  
}