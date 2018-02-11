package game.screen

import core.engine.entity.DynamicEntity
import game.entity.ui.BasicWindow
import core.jgml.utils.SVec4
import game.entity.InventoryItem
import core.engine.entity.EntityFactory
import game.entity.ui.inventory.WarehouseSlot
import game.entity.ui.Button
import game.entity.weapon.Weapon
import game.entity.weapon.Thrown
import game.entity.armor.Armor
import game.entity.Backpack
import core.engine.messages.Clicked

case class AddItem(item: InventoryItem, quantity: Int)
class Warehouse extends DynamicEntity {
  val weapons = collection.mutable.Map[InventoryItem, (Int, WarehouseSlot)]()
  val armor = collection.mutable.Map[InventoryItem, (Int, WarehouseSlot)]()
  val misc = collection.mutable.Map[InventoryItem, (Int, WarehouseSlot)]()
  val window: BasicWindow = new BasicWindow(704, 300, 280, 512, "Warehouse")
  window.borderColor(SVec4(0.7f, 0.7f, 0.7f, 0.5f))
  window.headerColor(SVec4(0.95f, 0.95f, 0.8f, 0.7f))
  window.color(SVec4(0.8f, 0.85f, 0.8f, 0.7f))
  window.labelSize(18)
  val weaponsTabButton = new Button(6, 10, 80, 20, "Weapons")
  weaponsTabButton.label.color = SVec4(0, 0, 0, 1)
  weaponsTabButton.label.size = 16
  window += weaponsTabButton
  val armorTabButton = new Button(90, 10, 80, 20, "Armor")
  armorTabButton.label.color = SVec4(0, 0, 0, 1)
  armorTabButton.label.size = 16
  window += armorTabButton
  val miscTabButton = new Button(174, 10, 80, 20, "Misc")
  miscTabButton.label.color = SVec4(0, 0, 0, 1)
  miscTabButton.label.size = 16
  window += miscTabButton
  var currentTab = weapons

  def nextFrame(elapsedMs: Long) = {

  }

  /* (non-Javadoc)
 * @see core.engine.entity.DynamicEntity#handleMessage()
 */
  def handleMessage = {
    case AddItem(item: InventoryItem, quantity: Int) =>
      val slot = EntityFactory.create[WarehouseSlot]
      window += slot
      slot.item = item
      item match {
        case i: Weapon => addItemToTab(weapons, item, quantity, slot)
        case i: Armor => addItemToTab(armor, item, quantity, slot)
        case i: Thrown => addItemToTab(misc, item, quantity, slot) 
        case i: Backpack => addItemToTab(misc, item, quantity, slot)
      }
    case Clicked(Some(e), m, _) if e == weaponsTabButton && currentTab != weapons => showTab(weapons)
    case Clicked(Some(e), m, _) if e == armorTabButton && currentTab != armor => showTab(armor)
    case Clicked(Some(e), m, _) if e == miscTabButton && currentTab != misc => showTab(misc)
    case msg => 
  }

  def +=(item: InventoryItem, quantity: Int) = self ! AddItem(item, quantity)
  
  private def showTab(itemsMap: collection.mutable.Map[InventoryItem, (Int, WarehouseSlot)]) = {
    println("show tab")
    val tabs = List(weapons, armor, misc).filter(_ != itemsMap)
    tabs foreach {
      _ foreach {
        case (e, (q, slot)) => slot.hide
      }
    }
    itemsMap foreach {
      case (e, (q, slot)) => slot.show
    }
    currentTab = itemsMap
  }

  private def addItemToTab(itemsMap: collection.mutable.Map[InventoryItem, (Int, WarehouseSlot)], item: InventoryItem, quantity: Int, slot: WarehouseSlot): Unit = {
    slot.size(30, -(itemsMap.size * 110 + 130), 6, 3)
    itemsMap(item) = (quantity, slot)
    slot.addContent(item)
    if (currentTab != itemsMap)
      slot.hide
  }
  
  override def hide() = {
    super.hide
    window.hide
  }

  override def show() = {
    super.show
    window.show
    showTab(currentTab)
  }
}