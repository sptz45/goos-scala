package auctionsniper

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.SwingUtilities

import ui.MainWindow
import xmpp.XMPPAuctionHouse

class Main {
  val portfolio = new SniperPortfolio
  var ui: MainWindow = _
  
  startUserInterface()
  
  private def startUserInterface() {
    SwingUtilities.invokeAndWait(new Runnable() {
      def run() {
        ui = new MainWindow(portfolio)
      }
    })
  }

  private def disconnectWhenUICloses(auctionHouse: XMPPAuctionHouse) { 
    ui.addWindowListener(new WindowAdapter() { 
      override def windowClosed(e: WindowEvent) { 
        auctionHouse.disconnect()
      } 
    }) 
  } 

  private def addUserRequestListenerFor(auctionHouse: AuctionHouse) {
    ui.addUserRequestListener(new SniperLauncher(auctionHouse, portfolio))
  }
}

object Main {
  private val ARG_HOSTNAME = 0
  private val ARG_USERNAME = 1
  private val ARG_PASSWORD = 2
 
  def main(args: String*) {
    val main = new Main
    val auctionHouse = XMPPAuctionHouse.connect(args(ARG_HOSTNAME), args(ARG_USERNAME), args(ARG_PASSWORD)) 
    main.disconnectWhenUICloses(auctionHouse)
    main.addUserRequestListenerFor(auctionHouse)
  }
}

