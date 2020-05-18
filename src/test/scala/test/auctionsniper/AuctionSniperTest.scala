package test.auctionsniper

import auctionsniper.SniperState._
import org.hamcrest.Matchers._
import org.junit.Assert.assertThat

import org.hamcrest.FeatureMatcher
import org.jmock._

import org.jmock.integration.junit4.JMock
import org.junit.{Before, Test}
import org.junit.runner.RunWith

import auctionsniper._
import auctionsniper.PriceSource
import auctionsniper.UserRequestListener.Item

object AuctionSniperTest { 
  protected val ITEM_ID = "item-id"
  val ITEM = new Item(ITEM_ID, 1234)
}

@RunWith(classOf[JMock]) 
class AuctionSniperTest {
  import AuctionSniperTest._
  
  private val context = new Mockery
  private val sniperState = context.states("sniper")
  private val auction = context.mock(classOf[Auction])
  private val sniperListener = context.mock(classOf[SniperListener])
  private val sniper = new AuctionSniper(ITEM, auction) 
  
  @Before
  def attachListener(): Unit = {
    sniper.addSniperListener(sniperListener)
  }
  
  @Test
  def hasInitialStateOfJoining(): Unit = {
    assertThat(sniper.snapshot, equalTo(SniperSnapshot.joining(ITEM_ID)))
  }
  
  @Test
  def reportsLostWhenAuctionClosesImmediately(): Unit = { 
    context.checking(new Expectations { 
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 0, 0, LOST))  
    }) 
    sniper.auctionClosed() 
  }
  
  @Test 
  def bidsHigherAndReportsBiddingWhenNewPriceArrives(): Unit = { 
    val price = 1001
    val increment = 25 
    val bid = price + increment
    context.checking(new Expectations { 
      oneOf(auction).bid(bid)

      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, BIDDING))
    }) 
    
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder) 
  } 
  

  @Test
  def doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice(): Unit = {
    val price = 1233
    val increment = 25

    context.checking(new Expectations {
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, 0, LOSING))
    })
    
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
  }

  @Test
  def doesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice(): Unit = {
    allowingSniperBidding()
    context.checking(new Expectations {
      private val bid = 123 + 45
      allowing(auction).bid(bid)
      
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 2345, bid, LOSING)); when(sniperState.is("bidding"))
    })
   
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
    sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder)
  }
  
  @Test
  def doesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice(): Unit = {
    val price = 1233
    val increment = 25

    allowingSniperBidding()
    allowingSniperWinning()
    context.checking(new Expectations {
      private val bid = 123 + 45
      allowing(auction).bid(bid)
      
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, LOSING)); when(sniperState.is("winning"))
    })
   
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
    sniper.currentPrice(168, 45, PriceSource.FromSniper)
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
  }

  @Test
  def continuesToBeLosingOnceStopPriceHasBeenReached(): Unit = {
    val states = context.sequence("sniper states")
    val price1 = 1233
    val price2 = 1258

    context.checking(new Expectations {
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price1, 0, LOSING)); inSequence(states)
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price2, 0, LOSING)); inSequence(states)
    })
   
    sniper.currentPrice(price1, 25, PriceSource.FromOtherBidder)
    sniper.currentPrice(price2, 25, PriceSource.FromOtherBidder)
  }

  @Test
  def reportsLostIfAuctionClosesWhenBidding(): Unit = { 
    allowingSniperBidding()
    ignoringAuction()
    
    context.checking(new Expectations {
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 168, LOST))
      when(sniperState.is("bidding"))
    })
    
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
    sniper.auctionClosed()
  } 
  
  @Test
  def reportsLostIfAuctionClosesWhenLosing(): Unit = {
    allowingSniperLosing()
    context.checking(new Expectations {
      atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 1230, 0, LOST))
      when(sniperState.is("losing"))
    })
    
    sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder)
    sniper.auctionClosed()
  }



  @Test 
  def reportsIsWinningWhenCurrentPriceComesFromSniper(): Unit = { 
    allowingSniperBidding()
    ignoringAuction()
    context.checking(new Expectations() {
      atLeast(1).of(sniperListener).sniperStateChanged( new SniperSnapshot(ITEM_ID, 135, 135, WINNING)); when(sniperState.is("bidding"))
    })
    
    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
    sniper.currentPrice(135, 45, PriceSource.FromSniper)
  } 
  
  @Test 
  def reportsWonIfAuctionClosesWhenWinning(): Unit = { 
    allowingSniperBidding()
    allowingSniperWinning()
    ignoringAuction()
    
    context.checking(new Expectations() { 
      atLeast(1).of(sniperListener).sniperStateChanged( new SniperSnapshot(ITEM_ID, 135, 135, WON)); when(sniperState.is("winning"))
    })
    
    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
    sniper.currentPrice(135, 45, PriceSource.FromSniper)
    sniper.auctionClosed()
  } 

  @Test 
  def reportsFailedIfAuctionFailsWhenBidding(): Unit = { 
    ignoringAuction()
    allowingSniperBidding()
    
    expectSniperToFailWhenItIs("bidding")
    
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder) 
    sniper.auctionFailed() 
  } 
  
  @Test
  def reportsFailedIfAuctionFailsImmediately(): Unit = {
    context.checking(new Expectations {
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot.joining(ITEM_ID).failed())
    })
    
    sniper.auctionFailed()
  }

  @Test
  def reportsFailedIfAuctionFailsWhenLosing(): Unit = {
    allowingSniperLosing()

    expectSniperToFailWhenItIs("losing")
    
    sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder)
    sniper.auctionFailed()
  }


  @Test
  def reportsFailedIfAuctionFailsWhenWinning(): Unit = {
    ignoringAuction()
    allowingSniperBidding()
    allowingSniperWinning()
    
    expectSniperToFailWhenItIs("winning")
    
    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
    sniper.currentPrice(135, 45, PriceSource.FromSniper)
    sniper.auctionFailed()
  }


  private def expectSniperToFailWhenItIs(state: String): Unit = {
    context.checking(new Expectations {
      atLeast(1).of(sniperListener).sniperStateChanged(
          new SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED))
      when(sniperState.is(state))
    })
  }
  private def ignoringAuction(): Unit = {
    context.checking(new Expectations { 
      ignoring(auction)
    })
  }
  private def allowingSniperBidding(): Unit = {
    allowSniperStateChange(BIDDING, "bidding")
  }

  private def allowingSniperLosing(): Unit = {
    allowSniperStateChange(LOSING, "losing")
  }

  private def allowingSniperWinning(): Unit = {
    allowSniperStateChange(WINNING, "winning")
  }

  private def allowSniperStateChange(newState: SniperState, oldState: String): Unit = {
    context.checking(new Expectations { 
      allowing(sniperListener).sniperStateChanged(`with`(aSniperThatIs(newState))); `then`(sniperState.is(oldState))
    })
  }

  private def aSniperThatIs(state: SniperState) =  
    new FeatureMatcher[SniperSnapshot, SniperState](equalTo(state), "sniper that is ", "was") {
      override protected def featureValueOf(actual: SniperSnapshot): SniperState = actual.state
    } 
} 
