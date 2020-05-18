package test.endtoend.auctionsniper

import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

import org.hamcrest.Matchers._
import org.junit.Assert.assertThat
import org.hamcrest.Matcher
import org.jivesoftware.smack.{Chat, ChatManagerListener, MessageListener, XMPPConnection}
import org.jivesoftware.smack.packet.Message
import auctionsniper.xmpp.XMPPAuction

class FakeAuctionServer(val itemId: String) {
  import FakeAuctionServer._

  private val AUCTION_PASSWORD = "auction"
  private val messageListener = new SingleMessageListener
  private val connection = new XMPPConnection(XMPP_HOSTNAME)

  private var currentChat: Chat = null

  def startSellingItem(): Unit = {
    connection.connect()
    connection.login(ITEM_ID_AS_LOGIN.format(itemId), AUCTION_PASSWORD, AUCTION_RESOURCE)
    connection.getChatManager.addChatListener(new ChatManagerListener() {
      def chatCreated(chat: Chat, createdLocally: Boolean): Unit = {
        currentChat = chat
        chat.addMessageListener(messageListener)
      }
    })
  }

  def sendInvalidMessageContaining(brokenMessage: String): Unit ={
    currentChat.sendMessage(brokenMessage)
  } 

  def reportPrice(price: Int, increment: Int, bidder: String): Unit = {
    currentChat.sendMessage(
      "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;"
        .format(price, increment, bidder))
  }
  
  def hasReceivedJoinRequestFrom(sniperId: String): Unit = {
    receivesAMessageMatching(sniperId, equalTo(XMPPAuction.JOIN_COMMAND_FORMAT))
  } 
  
  def hasReceivedBid(bid: Int, sniperId: String): Unit = {
    receivesAMessageMatching(
      sniperId,
      equalTo(XMPPAuction.BID_COMMAND_FORMAT.format(bid))) 
  } 
  
  private def receivesAMessageMatching[T >: String](sniperId: String, messageMatcher: Matcher[T]): Unit = {
    messageListener.receivesAMessage(messageMatcher)
    assertThat(currentChat.getParticipant, equalTo(sniperId))
  } 
  
  def announceClosed(): Unit = { 
    currentChat.sendMessage("SOLVersion: 1.1; Event: CLOSE;")
  } 

  def stop(): Unit = {
    connection.disconnect()
  } 


  class SingleMessageListener extends MessageListener { 
    private val messages = new ArrayBlockingQueue[Message](1)
    
    def processMessage(chat: Chat, message: Message): Unit = { 
      messages.add(message) 
    } 
    
    def receivesAMessage(): Unit = {
      assertThat("Message", messages.poll(5, TimeUnit.SECONDS), is(notNullValue(classOf[Message])))
    }

    def receivesAMessage[T >: String](messageMatcher: Matcher[T]): Unit = {
      val message = messages.poll(5, TimeUnit.SECONDS)
      assertThat(message, hasProperty("body", messageMatcher))
    }
  }
}

object FakeAuctionServer {
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_RESOURCE = "Auction"
  val XMPP_HOSTNAME = "localhost"
}
