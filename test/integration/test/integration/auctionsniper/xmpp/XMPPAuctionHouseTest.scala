package test.integration.auctionsniper.xmpp

import java.util.concurrent.CountDownLatch
import org.jivesoftware.smack.XMPPException
import org.junit.{After, Before, Test}

import java.util.concurrent.TimeUnit.SECONDS
import org.junit.Assert.assertTrue

import test.endtoend.auctionsniper.ApplicationRunner
import test.endtoend.auctionsniper.FakeAuctionServer
import auctionsniper.{Auction, AuctionEventListener, PriceSource}
import auctionsniper.UserRequestListener.Item
import auctionsniper.xmpp.{XMPPAuctionException, XMPPAuctionHouse}

class XMPPAuctionHouseTest {
  
  private val auctionServer = new FakeAuctionServer("item-54321")  
  private var auctionHouse: XMPPAuctionHouse = _

  @Before
  def openConnection() {
    auctionHouse = XMPPAuctionHouse.connect(
      FakeAuctionServer.XMPP_HOSTNAME,
      ApplicationRunner.SNIPER_ID,
      ApplicationRunner.SNIPER_PASSWORD)
  }
  
  @After
  def closeConnection() {
    if (auctionHouse != null)
      auctionHouse.disconnect()
  }
  
  @Before
  def startAuction() {
    auctionServer.startSellingItem()
  }
  
  @After
  def stopAuction() {
    auctionServer.stop()
  }

  @Test
  def receivesEventsFromAuctionServerAfterJoining() { 
    val auctionWasClosed = new CountDownLatch(1) 
    
    val auction = auctionHouse.auctionFor(new Item(auctionServer.itemId, 567))
    auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed))
    auction.join()
    auctionServer.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID) 
    auctionServer.announceClosed()
    
    assertTrue("should have been closed", auctionWasClosed.await(4, SECONDS)) 
  } 

  private def auctionClosedListener(auctionWasClosed: CountDownLatch) =
    new AuctionEventListener() { 
      def auctionClosed() { auctionWasClosed.countDown() } 
      def currentPrice(price: Int, increment: Int, priceSource: PriceSource) { }
      def auctionFailed() { }
    }
}
