package auctionsniper

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.SwingUtilities

import ui.MainWindow
import xmpp.XMPPAuctionHouse

class Main:

  val portfolio = new SniperPortfolio
  var ui: MainWindow = _
  
  startUserInterface()
  
  private def startUserInterface(): Unit =
    SwingUtilities.invokeAndWait(() => ui = MainWindow(portfolio))

  private def disconnectWhenUICloses(auctionHouse: XMPPAuctionHouse): Unit =
    ui.addWindowListener(new WindowAdapter():
      override def windowClosed(e: WindowEvent): Unit = auctionHouse.disconnect()
    )

  private def addUserRequestListenerFor(auctionHouse: AuctionHouse): Unit =
    ui.addUserRequestListener(SniperLauncher(auctionHouse, portfolio))

object Main:
  @main
  def run(hostname: String, username: String, password:String): Unit =
    val main = Main()
    val auctionHouse = XMPPAuctionHouse.connect(hostname, username, password)
    main.disconnectWhenUICloses(auctionHouse)
    main.addUserRequestListenerFor(auctionHouse)

