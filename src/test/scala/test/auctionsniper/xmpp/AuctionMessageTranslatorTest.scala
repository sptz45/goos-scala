package test.auctionsniper.xmpp

import auctionsniper.xmpp.{AuctionMessageTranslator, XMPPFailureReporter}
import auctionsniper.{AuctionEventListener, PriceSource}
import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Message
import org.jmock.AbstractExpectations._
import org.jmock.Expectations
import test.fixtures.JMockSuite

class AuctionMessageTranslatorTest extends JMockSuite {

  private val SNIPER_ID = "sniper id"
  private val UNUSED_CHAT: Chat = null

  case class TestFixture(
    failureReporter: XMPPFailureReporter,
    listener: AuctionEventListener,
    translator: AuctionMessageTranslator
  )

  private val withFixture = FunFixture[TestFixture](
    setup = _ => {
      val failureReporter = context().mock(classOf[XMPPFailureReporter])
      val listener = context().mock(classOf[AuctionEventListener])
      val translator = new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter)
      TestFixture(failureReporter, listener, translator)
    },
    teardown = _ => ()
  )

  withFixture.test("notifies auction closed when close message received") { implicit fixture =>
    import fixture._
    context().checking(new Expectations {
      exactly(1).of(listener).auctionClosed() 
    }) 
  
    val msg = message("SOLVersion: 1.1; Event: CLOSE;")
    
    translator.processMessage(UNUSED_CHAT, msg)
  } 

  withFixture.test("notifies bid details when current price message received from other bidder") { implicit fixture =>
    import fixture._
    context().checking(new Expectations {
      exactly(1).of(listener).currentPrice(192, 7, PriceSource.FromOtherBidder) 
    }) 
    
    val msg = message("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")
    
    translator.processMessage(UNUSED_CHAT, msg)
  } 

  withFixture.test("notifies bid details when current price message received from sniper") { implicit fixture =>
    import fixture._
    context().checking(new Expectations {
      exactly(1).of(listener).currentPrice(192, 7, PriceSource.FromSniper) 
    })
    
    val msg = message("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: " + SNIPER_ID + ";")
    
    translator.processMessage(UNUSED_CHAT, msg)
  } 

  withFixture.test("notifies auction failed when bad message received") { implicit fixture =>
    import fixture._
    val badMessage = "a bad message"
    expectFailureWithMessage(badMessage)
    
    translator.processMessage(UNUSED_CHAT, message(badMessage)) 
  }

  withFixture.test("notifies auction failed when event type missing") { implicit fixture =>
    import fixture._
    val badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";"
    expectFailureWithMessage(badMessage)

    translator.processMessage(UNUSED_CHAT, message(badMessage)) 
  } 

  private def message(body: String) = { 
    val message = new Message
    message.setBody(body) 
    message
  } 
  
  private def expectFailureWithMessage(badMessage: String)(implicit fxt: TestFixture): Unit = {
    context().checking(new Expectations {
      oneOf(fxt.listener).auctionFailed()
      oneOf(fxt.failureReporter).cannotTranslateMessage(
                               `with`(SNIPER_ID), `with`(badMessage),
                               `with`(any(classOf[Exception])))
    })
  } 
}