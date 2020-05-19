package test.auctionsniper

import auctionsniper.UserRequestListener.Item
import auctionsniper._
import org.hamcrest.FeatureMatcher
import org.hamcrest.Matchers._
import org.jmock.AbstractExpectations._
import org.jmock.Expectations
import test.fixtures.JMockSuite

class SniperLauncherTest extends JMockSuite {

  test("adds new sniper to collector and then joins auction") {

    val auctionState = context().states("auction state").startsAs("not joined")
    val auction = context().mock(classOf[Auction])
    val auctionHouse = context().mock(classOf[AuctionHouse])
    val sniperCollector = context().mock(classOf[SniperCollector])
    val launcher = new SniperLauncher(auctionHouse, sniperCollector)

    val item = Item("item 123", 456)

    context().checking(new Expectations() {

      allowing(auctionHouse).auctionFor(item); will(returnValue(auction))

      oneOf(auction).addAuctionEventListener(`with`(sniperForItem(item))); when(auctionState.is("not joined"))
      oneOf(sniperCollector).addSniper(`with`(sniperForItem(item))); when(auctionState.is("not joined"))

      oneOf(auction).join(); `then`(auctionState.is("joined"))
    })
    
    launcher.joinAuction(item)
  }

  private def sniperForItem(item: Item) =
    new FeatureMatcher[AuctionSniper, String](equalTo(item.identifier), "sniper with item id", "item") {
      override protected def featureValueOf(actual: AuctionSniper): String = actual.snapshot.itemId
  }
}