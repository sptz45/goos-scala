package test.endtoend.auctionsniper

import auctionsniper.ui.MainWindow.{NEW_ITEM_ID_NAME, NEW_ITEM_STOP_PRICE_NAME}
import com.objogate.wl.swing.matcher.IterableComponentsMatcher.matching
import com.objogate.wl.swing.matcher.JLabelTextMatcher.withLabelText
import java.lang.String.valueOf

import javax.swing.{JButton, JTextField}
import javax.swing.table.JTableHeader
import com.objogate.wl.swing.AWTEventQueueProber
import com.objogate.wl.swing.driver.{JButtonDriver, JFrameDriver, JTableDriver, JTableHeaderDriver, JTextFieldDriver, ComponentDriver}
import com.objogate.wl.swing.gesture.GesturePerformer

import auctionsniper.ui.MainWindow

class AuctionSniperDriver(timeoutMillis: Int)
extends JFrameDriver(
  new GesturePerformer(), 
  JFrameDriver.topLevelFrame(
    ComponentDriver.named(MainWindow.MAIN_WINDOW_NAME),
    ComponentDriver.showingOnScreen()), 
    new AWTEventQueueProber(timeoutMillis, 100)) {
  
  def hasColumnTitles() {
    val headers = new JTableHeaderDriver(this, classOf[JTableHeader]) 
    headers.hasHeaders(
       matching(withLabelText("Item"), withLabelText("Last Price"), 
                withLabelText("Last Bid"), withLabelText("State")))
  }

  def showsSniperStatus(itemId: String, lastPrice: Int, lastBid: Int, statusText: String) {
    val table = new JTableDriver(this) 
    table.hasRow(
      matching(withLabelText(itemId), withLabelText(valueOf(lastPrice)), 
               withLabelText(valueOf(lastBid)), withLabelText(statusText)))
  }

  def startBiddingWithStopPrice(itemId: String, stopPrice: Int) {
    textField(NEW_ITEM_ID_NAME).replaceAllText(itemId)
    textField(NEW_ITEM_STOP_PRICE_NAME).replaceAllText(stopPrice.toString) 
    bidButton().click()
  }

  private def textField(fieldName: String) = {
    val newItemId = new JTextFieldDriver(this, classOf[JTextField], ComponentDriver.named(fieldName))
    newItemId.focusWithMouse()
    newItemId
  }

  private def bidButton() =
    new JButtonDriver(this, classOf[JButton], ComponentDriver.named(MainWindow.JOIN_BUTTON_NAME))
}
