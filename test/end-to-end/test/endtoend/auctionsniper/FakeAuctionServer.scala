package test.endtoend.auctionsniper

import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

import org.hamcrest.Matchers._
import org.junit.Assert.assertThat

import org.hamcrest.Matcher;
import org.jivesoftware.smack.{Chat, ChatManagerListener, MessageListener, XMPPConnection, XMPPException}
import org.jivesoftware.smack.packet.Message

import auctionsniper.xmpp.XMPPAuction

class FakeAuctionServer(val itemId: String) {
  import FakeAuctionServer._

  private val AUCTION_PASSWORD = "auction"
  private val messageListener = new SingleMessageListener
  private val connection = new XMPPConnection(XMPP_HOSTNAME)

  private var currentChat: Chat = null

  def startSellingItem() {
    connection.connect()
    connection.login(ITEM_ID_AS_LOGIN.format(itemId), AUCTION_PASSWORD, AUCTION_RESOURCE)
    connection.getChatManager.addChatListener(new ChatManagerListener() {
      def chatCreated(chat: Chat, createdLocally: Boolean) {
        currentChat = chat
        chat.addMessageListener(messageListener)
      }
    })
  }

  def sendInvalidMessageContaining(brokenMessage: String){
    currentChat.sendMessage(brokenMessage)
  } 

  def reportPrice(price: Int, increment: Int, bidder: String) {
    currentChat.sendMessage(
      "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;"
        .format(price, increment, bidder))
  }
  
  def hasReceivedJoinRequestFrom(sniperId: String) {
    receivesAMessageMatching(sniperId, equalTo(XMPPAuction.JOIN_COMMAND_FORMAT))
  } 
  
  def hasReceivedBid(bid: Int, sniperId: String) {
    receivesAMessageMatching(
      sniperId,
      equalTo(XMPPAuction.BID_COMMAND_FORMAT.format(bid))) 
  } 
  
  private def receivesAMessageMatching[T >: String](sniperId: String, messageMatcher: Matcher[T]) {
    messageListener.receivesAMessage(messageMatcher)
    assertThat(currentChat.getParticipant, equalTo(sniperId))
  } 
  
  def announceClosed() { 
    currentChat.sendMessage("SOLVersion: 1.1; Event: CLOSE;")
  } 

  def stop() {
    connection.disconnect()
  } 


  class SingleMessageListener extends MessageListener { 
    private val messages = new ArrayBlockingQueue[Message](1)
    
    def processMessage(chat: Chat, message: Message) { 
      messages.add(message) 
    } 
    
    def receivesAMessage() {
      assertThat("Message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue[Message]))
    }

    def receivesAMessage[T >: String](messageMatcher: Matcher[T]) {
      val message = messages.poll(5, TimeUnit.SECONDS)
      //XXX crashes the compiler: assertThat(message, hasProperty("body", messageMatcher))
      assertThatReplacementForBug2705(message, hasProperty("body", messageMatcher))
    }
    
    //XXX temporary workaround for bug: https://lampsvn.epfl.ch/trac/scala/ticket/2705
    private def assertThatReplacementForBug2705[A, M >: A](actual: A, matcher: Matcher[M]) {
      if (!matcher.matches(actual)) {
        val description = new org.hamcrest.StringDescription()
        description.appendText("\nExpected: ")
                   .appendDescriptionOf(matcher)
                   .appendText("\n     but: ")
        matcher.describeMismatch(actual, description)
        throw new AssertionError(description.toString)
      }
    }
  }
}

object FakeAuctionServer {
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_RESOURCE = "Auction"
  val XMPP_HOSTNAME = "localhost"
}
