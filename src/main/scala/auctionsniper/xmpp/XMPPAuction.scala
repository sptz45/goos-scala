package auctionsniper.xmpp

import org.jivesoftware.smack.{Chat, XMPPConnection, XMPPException}
import auctionsniper.{Auction, AuctionEventListener, PriceSource}
import auctionsniper.util.Announcer

class XMPPAuction(
  connection: XMPPConnection,
  auctionJID: String,
  failureReporter: XMPPFailureReporter) extends Auction {

  import XMPPAuction._

  private val auctionEventListeners = Announcer.to[AuctionEventListener]
  private val translator = translatorFor(connection)
  private val chat = connection.getChatManager.createChat(auctionJID, translator)
  addAuctionEventListener(chatDisconnectorFor(translator))
   
  def bid(amount: Int) { 
    sendMessage(BID_COMMAND_FORMAT.format(amount)) 
  } 
  
  def join() { 
    sendMessage(JOIN_COMMAND_FORMAT)
  }
  
  final def addAuctionEventListener(listener: AuctionEventListener) {
    auctionEventListeners += listener
  }
  
  private def translatorFor(connection: XMPPConnection) =
    new AuctionMessageTranslator(connection.getUser, auctionEventListeners.announce(), failureReporter)
  
  private def chatDisconnectorFor(translator: AuctionMessageTranslator) = new AuctionEventListener() { 
    def auctionFailed() { chat.removeMessageListener(translator) }
    def auctionClosed() { }
    def currentPrice(price: Int, increment: Int, priceSource: PriceSource) { } 
  }
  
  private def sendMessage(message: String) { 
    try {
      chat.sendMessage(message)
    } catch {
      case e: XMPPException => e.printStackTrace() 
    } 
  } 
}


object XMPPAuction {
  val JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;"
  val BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;"
}
