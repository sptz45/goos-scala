package test.auctionsniper.xmpp

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Message
import org.jmock.{Expectations, Mockery}
import org.jmock.integration.junit4.JMock
import org.junit.Test
import org.junit.runner.RunWith

import org.jmock.Expectations._

import auctionsniper.{AuctionEventListener, PriceSource}
import auctionsniper.xmpp.{AuctionMessageTranslator, XMPPFailureReporter}

@RunWith(classOf[JMock]) 
class AuctionMessageTranslatorTest { 
  private val SNIPER_ID = "sniper id"
  private val UNUSED_CHAT: Chat = null
  
  private val context = new Mockery 
  private val failureReporter = context.mock(classOf[XMPPFailureReporter])
  private val listener = context.mock(classOf[AuctionEventListener])
  private val translator = new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter)
  
  @Test 
  def notifiesAuctionClosedWhenCloseMessageReceived() { 
    context.checking(new Expectations { 
      exactly(1).of(listener).auctionClosed() 
    }) 
  
    val message = new Message 
    message.setBody("SOLVersion: 1.1; Event: CLOSE;") 
    
    translator.processMessage(UNUSED_CHAT, message) 
  } 
  
  @Test
  def notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() { 
    context.checking(new Expectations { 
      exactly(1).of(listener).currentPrice(192, 7, PriceSource.FromOtherBidder) 
    }) 
    
    val message = new Message 
    message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;")
    
    translator.processMessage(UNUSED_CHAT, message)
  } 

  @Test
  def notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() { 
    context.checking(new Expectations { 
      exactly(1).of(listener).currentPrice(192, 7, PriceSource.FromSniper) 
    })
    
    val message = new Message 
    message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: " + SNIPER_ID + ";")
    
    translator.processMessage(UNUSED_CHAT, message)
  } 

  @Test 
  def notifiesAuctionFailedWhenBadMessageReceived() { 
    val badMessage = "a bad message"
    expectFailureWithMessage(badMessage)
    
    translator.processMessage(UNUSED_CHAT, message(badMessage)) 
  } 

  @Test 
  def notifiesAuctionFailedWhenEventTypeMissing() { 
    val badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";"
    expectFailureWithMessage(badMessage)

    translator.processMessage(UNUSED_CHAT, message(badMessage)) 
  } 

  private def message(body: String) = { 
    val message = new Message 
    message.setBody(body) 
    message
  } 
  
  private def expectFailureWithMessage(badMessage: String) { 
    context.checking(new Expectations {  
      oneOf(listener).auctionFailed() 
      oneOf(failureReporter).cannotTranslateMessage( 
                               `with`(SNIPER_ID), `with`(badMessage), 
                               `with`(any(classOf[Exception])))
    }) 
  } 
}