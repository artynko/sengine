package core.engine.userinput

import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import core.app.Component
import java.awt.event.MouseListener
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.event.KeyListener
import java.awt.event.KeyEvent

class UserInputListener extends MouseListener with MouseMotionListener with KeyListener with Component {
  var eventReceived: Option[MouseEvent] = None
  var mouseClicked: Option[MouseEvent] = None
  // guid, button modifiers
  var clicked: Option[(Int, Int)] = None
  // guid, button modifiers
  var pressed: Option[(Int, Int)] = None
  // guid, button modifiers
  var released: Option[(Int, Int)] = None
  // guid, button modifiers, x, y
  var moved: Option[(Int, Int, Int, Int)] = None
  var lastMovedX = 0
  var mouseDragged: Option[MouseEvent] = None
  var mousePressed: Option[MouseEvent] = None
  var mouseReleased: Option[MouseEvent] = None
  var mouseMoved: Option[MouseEvent] = None
  var keyPressed: Option[String] = None
  def actionPerformed(action: ActionEvent) = {
  }
  def mouseClicked(event: MouseEvent) = {
    mouseClicked = Some(event)
    eventReceived = Some(event) // I don't really care about what the type in here is since, the render pickup program only uses x, y cooridnates to do its stuff
  }
  def mouseEntered(event: MouseEvent) = {}
  def mouseExited(event: MouseEvent) = {}
  def mousePressed(event: MouseEvent) = {
    lastMovedX = event.getX()
    mousePressed = Some(event)
    eventReceived = Some(event) // I don't really care about what the type in here is since, the render pickup program only uses x, y cooridnates to do its stuff
  }
  def mouseReleased(event: MouseEvent) = {
    mouseReleased = Some(event)
    eventReceived = Some(event) // I don't really care about what the type in here is since, the render pickup program only uses x, y cooridnates to do its stuff
  }
  def mouseDragged(event: MouseEvent) = {
    //println(event)
    mouseDragged = Some(event)
    eventReceived = Some(event) // I don't really care about what the type in here is since, the render pickup program only uses x, y cooridnates to do its stuff
  }
  def mouseMoved(event: MouseEvent) = {
    mouseMoved = Some(event)
    eventReceived = Some(event) // I don't really care about what the type in here is since, the render pickup program only uses x, y cooridnates to do its stuff
  }
  def keyPressed(event: KeyEvent) = {}
  def keyReleased(event: KeyEvent) = {}
  def keyTyped(event: KeyEvent) = {
    event.getKeyChar match {
      case KeyEvent.VK_TAB => keyPressed = Some("TAB")
      case e => keyPressed = Some(e.toString)
    }
  }

}