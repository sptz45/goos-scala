package auctionsniper.xmpp

import org.jivesoftware.smack.{XMPPConnection, XMPPException}
import auctionsniper.{Auction, AuctionEventListener, PriceSource}
import auctionsniper.util.Announcer

class XMPPAuction(
  connection: XMPPConnection,
  auctionJID: String,
  failureReporter: XMPPFailureReporter) extends Auction:

  import XMPPAuction.*

  private val auctionEventListeners = Announcer.to[AuctionEventListener]
  private val translator = translatorFor(connection)
  private val chat = connection.getChatManager.createChat(auctionJID, translator)
  addAuctionEventListener(chatDisconnectorFor(translator))
   
  def bid(amount: Int): Unit =
    sendMessage(BID_COMMAND_FORMAT.format(amount))
  
  def join(): Unit =
    sendMessage(JOIN_COMMAND_FORMAT)
  
  final def addAuctionEventListener(listener: AuctionEventListener): Unit =
    auctionEventListeners += listener
  
  private def translatorFor(connection: XMPPConnection) =
    AuctionMessageTranslator(connection.getUser, auctionEventListeners.announce(), failureReporter)
  
  private def chatDisconnectorFor(translator: AuctionMessageTranslator) = new AuctionEventListener():
    def auctionFailed(): Unit = chat.removeMessageListener(translator)
    def auctionClosed(): Unit = ()
    def currentPrice(price: Int, increment: Int, priceSource: PriceSource): Unit = ()
  
  private def sendMessage(message: String): Unit =
    try chat.sendMessage(message)
    catch case e: XMPPException => e.printStackTrace()


object XMPPAuction:
  val JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;"
  val BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;"
