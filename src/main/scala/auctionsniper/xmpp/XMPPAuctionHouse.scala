package auctionsniper.xmpp

import auctionsniper.UserRequestListener.Item
import auctionsniper.{Auction, AuctionHouse}
import org.apache.commons.io.FilenameUtils
import org.jivesoftware.smack.{XMPPConnection, XMPPException}

import java.util.logging.{FileHandler, Logger, SimpleFormatter}
import scala.util.chaining.*

class XMPPAuctionHouse(connection: XMPPConnection) extends AuctionHouse:
  import XMPPAuctionHouse.*
  
  private val failureReporter = LoggingXMPPFailureReporter(makeLogger())
  
  def disconnect(): Unit =
    connection.disconnect()
  
  def auctionFor(item: Item): Auction =
    new XMPPAuction(connection, auctionId(item.identifier, connection), failureReporter)
  
  private def makeLogger() =
    Logger.getLogger(LOGGER_NAME).tap { logger =>
      logger.setUseParentHandlers(false)
      logger.addHandler(simpleFileHandler())
    }
  
  private def simpleFileHandler() =
    try
      FileHandler(LOG_FILE_NAME).tap(_.setFormatter(SimpleFormatter()))
    catch
      case e: Exception =>
        throw XMPPAuctionException(
          "Could not create logger FileHandler " + FilenameUtils.getFullPath(LOG_FILE_NAME), e)


object XMPPAuctionHouse:
  val LOGGER_NAME = "auction-sniper"
  val LOG_FILE_NAME = "auction-sniper.log"
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_RESOURCE = "Auction"
  val AUCTION_ID_FORMAT: String = ITEM_ID_AS_LOGIN + "@%s/" + XMPPAuctionHouse.AUCTION_RESOURCE

  def auctionId(itemId: String, connection: XMPPConnection): String =
    AUCTION_ID_FORMAT.format(itemId, connection.getServiceName)

  def connect(hostname: String, username: String, password:String): XMPPAuctionHouse =
    val connection = XMPPConnection(hostname)
    try
      connection.connect()
      connection.login(username, password, AUCTION_RESOURCE) 
      XMPPAuctionHouse(connection)
    catch
      case xmppe: XMPPException =>
        throw XMPPAuctionException("Could not connect to auction: " + connection, xmppe)
