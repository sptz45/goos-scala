package test.auctionsniper

import auctionsniper.SniperState.*
import org.hamcrest.Matchers.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.FeatureMatcher
import org.jmock.*
import auctionsniper.*
import auctionsniper.PriceSource
import auctionsniper.UserRequestListener.Item
import test.fixtures.JMockSuite


class AuctionSniperTest  extends JMockSuite:

  private val ITEM_ID = "item-id"
  private val ITEM: Item = Item(ITEM_ID, 1234)

  case class TestFixture(
    sniperState: States,
    auction: Auction,
    sniperListener: SniperListener,
    sniper: AuctionSniper
  )

  private val withFixture = FunFixture[TestFixture](
    setup = _ => {

      val sniperState = context().states("sniper")
      val auction = context().mock(classOf[Auction])
      val sniperListener = context().mock(classOf[SniperListener])
      val sniper = AuctionSniper(ITEM, auction)

      sniper.addSniperListener(sniperListener)

      TestFixture(sniperState, auction, sniperListener, sniper)
    },
    teardown = _ => ()
  )


  withFixture.test("has initial state of joining") { implicit fixture =>
    import fixture.*
    assertThat(sniper.snapshot, equalTo(SniperSnapshot.joining(ITEM_ID)))
  }

  withFixture.test("reports lost when auction closes immediately") { implicit fixture =>
    import fixture.*
    context().checking(new Expectations:
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, 0, 0, LOST))
    )
    sniper.auctionClosed() 
  }
  

  withFixture.test("bids higher and reports bidding when new price arrives") { implicit fixture =>
    import fixture.*
    val price = 1001
    val increment = 25 
    val bid = price + increment
    context().checking(new Expectations:
      oneOf(auction).bid(bid)
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, price, bid, BIDDING))
    )
    
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder) 
  } 
  

  withFixture.test("doesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice") { implicit fixture =>
    import fixture.*
    val price = 1233
    val increment = 25

    context().checking(new Expectations:
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, price, 0, LOSING))
    )
    
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
  }

  withFixture.test("does not bid and reports losing if subsequent price is above stop price") { implicit fixture =>
    import fixture.*
    allowingSniperBidding()
    context().checking(new Expectations:
      private val bid = 123 + 45
      allowing(auction).bid(bid)
      
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, 2345, bid, LOSING))
      when(sniperState.is("bidding"))
    )
   
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
    sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder)
  }

  withFixture.test("does not bid and reports losing if price after winning is above stop price") { implicit fixture =>
    import fixture.*
    val price = 1233
    val increment = 25

    allowingSniperBidding()
    allowingSniperWinning()
    context().checking(new Expectations:
      private val bid = 123 + 45
      allowing(auction).bid(bid)
      
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, price, bid, LOSING))
      when(sniperState.is("winning"))
    )
   
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
    sniper.currentPrice(168, 45, PriceSource.FromSniper)
    sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
  }

  withFixture.test("continues to be losing once stop price has been reached") { implicit fixture =>
    import fixture.*
    val states = context().sequence("sniper states")
    val price1 = 1233
    val price2 = 1258

    context().checking(new Expectations:
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, price1, 0, LOSING)); inSequence(states)
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, price2, 0, LOSING)); inSequence(states)
    )
   
    sniper.currentPrice(price1, 25, PriceSource.FromOtherBidder)
    sniper.currentPrice(price2, 25, PriceSource.FromOtherBidder)
  }

  withFixture.test("reports lost if auction closes when bidding") { implicit fixture =>
    import fixture.*
    allowingSniperBidding()
    ignoringAuction()
    
    context().checking(new Expectations:
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, 123, 168, LOST))
      when(sniperState.is("bidding"))
    )
    
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
    sniper.auctionClosed()
  } 

  withFixture.test("reports lost if auction closes when losing") { implicit fixture =>
    import fixture.*
    allowingSniperLosing()
    context().checking(new Expectations:
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, 1230, 0, LOST))
      when(sniperState.is("losing"))
    )
    
    sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder)
    sniper.auctionClosed()
  }

  withFixture.test("reports is winning when current price comes from sniper") { implicit fixture =>
    import fixture.*
    allowingSniperBidding()
    ignoringAuction()
    context().checking(new Expectations():
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, 135, 135, WINNING))
      when(sniperState.is("bidding"))
    )
    
    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
    sniper.currentPrice(135, 45, PriceSource.FromSniper)
  } 
  

  withFixture.test("reports won if auction closes when winning") { implicit fixture =>
    import fixture.*
    allowingSniperBidding()
    allowingSniperWinning()
    ignoringAuction()
    
    context().checking(new Expectations():
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot(ITEM_ID, 135, 135, WON))
      when(sniperState.is("winning"))
    )
    
    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
    sniper.currentPrice(135, 45, PriceSource.FromSniper)
    sniper.auctionClosed()
  } 


  withFixture.test("reports failed if auction fails when bidding") { implicit fixture =>
    import fixture.*
    ignoringAuction()
    allowingSniperBidding()
    
    expectSniperToFailWhenItIs("bidding")
    
    sniper.currentPrice(123, 45, PriceSource.FromOtherBidder) 
    sniper.auctionFailed() 
  } 

  withFixture.test("reports failed if auction fails immediately") { implicit fixture =>
    import fixture.*
    context().checking(new Expectations:
      atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot.joining(ITEM_ID).failed())
    )
    
    sniper.auctionFailed()
  }

  withFixture.test("reports failed if auction fails when losing") { implicit fixture =>
    import fixture.*
    allowingSniperLosing()

    expectSniperToFailWhenItIs("losing")
    
    sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder)
    sniper.auctionFailed()
  }


  withFixture.test("reports failed if auction fails when winning") { implicit fixture =>
    import fixture.*
    ignoringAuction()
    allowingSniperBidding()
    allowingSniperWinning()
    
    expectSniperToFailWhenItIs("winning")
    
    sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
    sniper.currentPrice(135, 45, PriceSource.FromSniper)
    sniper.auctionFailed()
  }

  private def expectSniperToFailWhenItIs(state: String)(implicit f: TestFixture): Unit =
    context().checking(new Expectations:
      atLeast(1).of(f.sniperListener).sniperStateChanged(
          SniperSnapshot(ITEM_ID, 0, 0, SniperState.FAILED))
      when(f.sniperState.is(state))
    )
  private def ignoringAuction()(implicit f: TestFixture): Unit =
    context().checking(new Expectations:
      ignoring(f.auction)
    )
  private def allowingSniperBidding()(implicit f: TestFixture): Unit =
    allowSniperStateChange(BIDDING, "bidding")

  private def allowingSniperLosing()(implicit f: TestFixture): Unit =
    allowSniperStateChange(LOSING, "losing")

  private def allowingSniperWinning()(implicit f: TestFixture): Unit =
    allowSniperStateChange(WINNING, "winning")

  private def allowSniperStateChange(newState: SniperState, oldState: String)(implicit f: TestFixture): Unit =
    context().checking(new Expectations:
      allowing(f.sniperListener).sniperStateChanged(`with`(aSniperThatIs(newState))); `then`(f.sniperState.is(oldState))
    )

  private def aSniperThatIs(state: SniperState) =
    new FeatureMatcher[SniperSnapshot, SniperState](equalTo(state), "sniper that is ", "was"):
      override protected def featureValueOf(actual: SniperSnapshot): SniperState = actual.state

