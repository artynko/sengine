package game.entity.ui

import com.hackoeur.jglm.Vec4
import core.engine.entity.EntityFactory
import core.engine.entity.StaticEntity
import core.engine.entity.UiElement2D
import core.engine.entity.primitive.PrimitiveBox2D
import core.engine.entity.Entity
import core.engine.entity.Rendered
import game.entity.HeavyMg
import game.entity.ui.inventory.InventoryBackground
import core.render.AlphaTexture

class BasicWindow(bx: Int, by: Int, width: Int, height: Int, headerLabel: String = "header", border: Boolean = false, borderSize: Int = 0) {
  println("basicWindow created: " + this)
  val headerLabelEntity = EntityFactory.createStatic[Label]
  headerLabelEntity.text = headerLabel
  headerLabelEntity.size = 12
  headerLabelEntity.x = bx + borderSize + 2 + 5
  headerLabelEntity.y = by + height - (borderSize + headerLabelEntity.size + (headerLabel.size / 3).toInt) - 5

  val children = collection.mutable.ListBuffer[Entity]()

  // header
  val header = new Rectangle(0, 0, 0, 0)
  header.hide
  header.bottomLeft = (bx + borderSize, by + height - (borderSize + headerLabelEntity.size + (headerLabel.size / 3).toInt * 2))
  header.topRight = (bx + width - borderSize, by + height - borderSize)
  header.color = new Vec4(0.8f, 0.8f, 0.8f, 0.8f)
  header.zIndex = 0
  // inner
  val innerArea = new InventoryBackground(0, 0, 0, 0)
  innerArea.bottomLeft = (bx + borderSize, by + borderSize)
  innerArea.topRight = (bx + width - borderSize, by + height - borderSize)
  innerArea.color = new Vec4(0.3f, 0.3f, 0.3f, 0.9f)
  // boarder
  val bordere: Option[Rectangle] = borderSize match {
    case n if n > 0 =>
      val b = new Rectangle(0, 0, 0, 0) with AlphaTexture {
        val alphaTextureName = "textures/60alpha.bmp"
      }
      b.bottomLeft = (bx, by)
      b.topRight = (bx + width, by + height)
      b.color = new Vec4(0.0f, 0.0f, 0.0f, 0.6f)
      Some(b)
    case _ => None
  }

  def labelText(text: String) = headerLabelEntity.text = text
  def labelSize(size: Int) = headerLabelEntity.size = size
  def headerColor(c: Vec4) = header.color = c
  def color(c: Vec4) = innerArea.color = c
  def borderColor(c: Vec4) = for (b <- bordere) b.color = c
  def +=(entity: Entity) = entity match {
    case e: Entity with PrimitiveBox2D =>
      e.move(bx, by + height - (header.height + (e.topRight._2 - e.bottomLeft._2) + e.bottomLeft._2 * 2), 0)
      children += e
    case e: StaticEntity => children += e
    case _ => throw new RuntimeException("unknonw entity added to BasicWindow")
  }

  def clear() = children.clear

  def hide() = {
    headerLabelEntity.hide
    innerArea.hide
    bordere match {
      case Some(e) => e.hide
      case _ =>
    }
    children foreach {
      case c: Rendered => c.hide
      case _ =>
    }
  }
  def show() = {
    headerLabelEntity.show
    innerArea.show
    bordere match {
      case Some(e) => e.show
      case _ =>
    }
    children foreach {
      case c: Rendered => c.show
      case _ =>
    }
  }
}