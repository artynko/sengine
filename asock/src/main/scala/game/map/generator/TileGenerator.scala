package game.map.generator

trait TileGenerator {
  /**
   * Current is the current tile that was just resolved, next is one of the next tiles around current that is being resolved
   */
  def handleNextTile(current: ModelTile, next: ModelTile)

}