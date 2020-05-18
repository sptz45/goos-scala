package test.auctionsniper

import org.hamcrest.FeatureMatcher
import org.jmock.{Expectations, Mockery, States}
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hamcrest.Matchers._
import org.jmock.AbstractExpectations._

import auctionsniper._
import auctionsniper.UserRequestListener.Item;

@RunWith(classOf[JMock])
class SniperLauncherTest {
  
  private val context = new Mockery
  private val auctionState = context.states("auction state").startsAs("not joined")
  private val auction = context.mock(classOf[Auction])
  private val auctionHouse = context.mock(classOf[AuctionHouse])
  private val sniperCollector = context.mock(classOf[SniperCollector])
  private val launcher = new SniperLauncher(auctionHouse, sniperCollector)
  
  @Test
  def addsNewSniperToCollectorAndThenJoinsAuction() {
    val item = new Item("item 123", 456)

    context.checking(new Expectations() {
      allowing(auctionHouse).auctionFor(item); will(returnValue(auction));
      
      oneOf(auction).addAuctionEventListener(`with`(sniperForItem(item))); when(auctionState.is("not joined"));
      oneOf(sniperCollector).addSniper(`with`(sniperForItem(item))); when(auctionState.is("not joined"));
      
      one(auction).join(); then(auctionState.is("joined"));
    })
    
    launcher.joinAuction(item)
  }

  private def sniperForItem(item: Item) =
    new FeatureMatcher[AuctionSniper, String](equalTo(item.identifier), "sniper with item id", "item") {
      override protected def featureValueOf(actual: AuctionSniper) = actual.snapshot.itemId
  }
}