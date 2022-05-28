package test.integration.auctionsniper.xmpp

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

import auctionsniper.UserRequestListener.Item
import auctionsniper.xmpp.XMPPAuctionHouse
import auctionsniper.{AuctionEventListener, PriceSource}
import munit.FunSuite
import test.endtoend.auctionsniper.{ApplicationRunner, FakeAuctionServer}

class XMPPAuctionHouseTest extends FunSuite:

  private val fixture = FunFixture[(FakeAuctionServer, XMPPAuctionHouse)](
    setup = _ => {
      val auctionServer = FakeAuctionServer("item-54321")
      val auctionHouse = XMPPAuctionHouse.connect(
        FakeAuctionServer.XMPP_HOSTNAME, ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD)
      auctionServer.startSellingItem()
      auctionServer -> auctionHouse
    },
    teardown = {
      case (server, auctionHouse) =>
        auctionHouse.disconnect()
        server.stop()
    }
  )

  fixture.test("receives events from auction server after joining") { case (auctionServer, auctionHouse) =>
    val auctionWasClosed = CountDownLatch(1)
    
    val auction = auctionHouse.auctionFor(Item(auctionServer.itemId, 567))
    auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed))
    auction.join()
    auctionServer.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID) 
    auctionServer.announceClosed()

    assert(auctionWasClosed.await(4, SECONDS), "should have been closed")
  } 

  private def auctionClosedListener(auctionWasClosed: CountDownLatch) =
    new AuctionEventListener():
      def auctionClosed(): Unit = auctionWasClosed.countDown()
      def currentPrice(price: Int, increment: Int, priceSource: PriceSource): Unit = ()
      def auctionFailed(): Unit = ()
