package test.endtoend.auctionsniper

import java.io.IOException
import javax.swing.SwingUtilities
import org.hamcrest.Matchers.containsString

import auctionsniper.{Main, SniperState}
import auctionsniper.ui.MainWindow
import auctionsniper.ui.SnipersTableModel.textFor
import FakeAuctionServer.XMPP_HOSTNAME

object ApplicationRunner {
  val SNIPER_ID = "sniper" 
  val SNIPER_PASSWORD = "sniper"
  val SNIPER_XMPP_ID = SNIPER_ID + "@" + XMPP_HOSTNAME + "/Auction"
}


class ApplicationRunner {
  import ApplicationRunner._
  
  private val logDriver = new AuctionLogDriver
  private var driver: AuctionSniperDriver = null 
  
  def startBiddingIn(auctions: FakeAuctionServer*) {
    startSniper()
    auctions.foreach(openBiddingFor(_, Integer.MAX_VALUE))
  }
  
  def startBiddingWithStopPrice(auction: FakeAuctionServer, stopPrice: Int) {
    startSniper()
    openBiddingFor(auction, stopPrice)
  }  

  def hasShownSniperHasLostAuction(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.LOST))
  } 
  
  def hasShownSniperIsBidding(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.BIDDING))
  }

  def hasShownSniperIsWinning(auction: FakeAuctionServer, winningBid: Int) {
    driver.showsSniperStatus(auction.itemId, winningBid, winningBid, textFor(SniperState.WINNING))
  }

  def hasShownSniperIsLosing(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.LOSING));
  }

  def hasShownSniperHasWonAuction(auction: FakeAuctionServer, lastPrice: Int) {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastPrice, textFor(SniperState.WON));
  }

  def hasShownSniperHasFailed(auction: FakeAuctionServer) {
    driver.showsSniperStatus(auction.itemId, 0, 0, textFor(SniperState.FAILED))
  }

  def reportsInvalidMessage(auction: FakeAuctionServer, brokenMessage: String) {
    logDriver.hasEntry(containsString(brokenMessage))
  }

  def stop() { 
    if (driver != null) driver.dispose()
  }

  private def startSniper() {
    logDriver.clearLog()
    val thread = new Thread("Test Application") { 
      override def run() {  
        try { 
          Main.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD)
        } catch { 
          case e => e.printStackTrace()  
        } 
      } 
    }
    thread.setDaemon(true)
    thread.start()
    makeSureAwtIsLoadedBeforeStartingTheDriverOnOSXToStopDeadlock()
    
    driver = new AuctionSniperDriver(1000)
    driver.hasTitle(MainWindow.APPLICATION_TITLE)
    driver.hasColumnTitles()
  } 

  private def openBiddingFor(auction: FakeAuctionServer, stopPrice: Int) {
    val itemId = auction.itemId
    driver.startBiddingWithStopPrice(itemId, stopPrice)
    driver.showsSniperStatus(itemId, 0, 0, textFor(SniperState.JOINING))
  }

  private def makeSureAwtIsLoadedBeforeStartingTheDriverOnOSXToStopDeadlock() {
    try {
      SwingUtilities.invokeAndWait(new Runnable() { def run() {} })
    } catch {
      case e => throw new AssertionError(e)
    }
  }
}