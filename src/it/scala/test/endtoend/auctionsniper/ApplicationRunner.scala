package test.endtoend.auctionsniper

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
  
  def startBiddingIn(auctions: FakeAuctionServer*): Unit = {
    startSniper()
    auctions.foreach(openBiddingFor(_, Integer.MAX_VALUE))
  }
  
  def startBiddingWithStopPrice(auction: FakeAuctionServer, stopPrice: Int): Unit = {
    startSniper()
    openBiddingFor(auction, stopPrice)
  }  

  def hasShownSniperHasLostAuction(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int): Unit = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.LOST))
  } 
  
  def hasShownSniperIsBidding(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int): Unit = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.BIDDING))
  }

  def hasShownSniperIsWinning(auction: FakeAuctionServer, winningBid: Int): Unit = {
    driver.showsSniperStatus(auction.itemId, winningBid, winningBid, textFor(SniperState.WINNING))
  }

  def hasShownSniperIsLosing(auction: FakeAuctionServer, lastPrice: Int, lastBid: Int): Unit = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.LOSING));
  }

  def hasShownSniperHasWonAuction(auction: FakeAuctionServer, lastPrice: Int): Unit = {
    driver.showsSniperStatus(auction.itemId, lastPrice, lastPrice, textFor(SniperState.WON));
  }

  def hasShownSniperHasFailed(auction: FakeAuctionServer): Unit = {
    driver.showsSniperStatus(auction.itemId, 0, 0, textFor(SniperState.FAILED))
  }

  def reportsInvalidMessage(auction: FakeAuctionServer, brokenMessage: String): Unit = {
    logDriver.hasEntry(containsString(brokenMessage))
  }

  def stop(): Unit = { 
    if (driver != null) driver.dispose()
  }

  private def startSniper(): Unit = {
    logDriver.clearLog()
    val thread = new Thread("Test Application") { 
      override def run(): Unit = {  
        try { 
          Main.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD)
        } catch { 
          case e: Exception => e.printStackTrace()
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

  private def openBiddingFor(auction: FakeAuctionServer, stopPrice: Int): Unit = {
    val itemId = auction.itemId
    driver.startBiddingWithStopPrice(itemId, stopPrice)
    driver.showsSniperStatus(itemId, 0, 0, textFor(SniperState.JOINING))
  }

  private def makeSureAwtIsLoadedBeforeStartingTheDriverOnOSXToStopDeadlock(): Unit = {
    try {
      SwingUtilities.invokeAndWait(new Runnable() { def run(): Unit = {} })
    } catch {
      case e: Exception => throw new AssertionError(e)
    }
  }
}