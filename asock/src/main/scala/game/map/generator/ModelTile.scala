package game.map.generator

import core.engine.Point

/**
 * A tile that is used in the generator model, has info about local constrains etc.
 */
class ModelTile(var _tileType: GlobalTileType, var createdBy: ModelTile, var location: Point, var possibleTileTypes: List[GlobalTileType]) {
  def tileType = _tileType
  def tileType_=(t: GlobalTileType) = _tileType = t
  /**
   * Sets this model tile to have only single possible tile type
   */
  def singlePossibleType(t: GlobalTileType) = possibleTileTypes = List(t)
  
}