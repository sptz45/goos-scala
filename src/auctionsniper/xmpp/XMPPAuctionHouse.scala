package auctionsniper.xmpp

import org.apache.commons.io.FilenameUtils
import org.jivesoftware.smack.{XMPPConnection, XMPPException}
import java.util.logging.{FileHandler, Logger, SimpleFormatter}

import auctionsniper.{Auction, AuctionHouse}
import auctionsniper.UserRequestListener.Item

class XMPPAuctionHouse(connection: XMPPConnection) extends AuctionHouse {
  import XMPPAuctionHouse._
  
  private val failureReporter = new LoggingXMPPFailureReporter(makeLogger())
  
  def disconnect() {
    connection.disconnect()
  }
  
  def auctionFor(item: Item): Auction =
    new XMPPAuction(connection, auctionId(item.identifier, connection), failureReporter)
  
  private def makeLogger() = {
    val logger = Logger.getLogger(LOGGER_NAME) 
    logger.setUseParentHandlers(false)
    logger.addHandler(simpleFileHandler())
    logger
  }
  
  private def simpleFileHandler() = { 
    try {
      val handler = new FileHandler(LOG_FILE_NAME) 
      handler.setFormatter(new SimpleFormatter)
      handler
    } catch {
      case e: Exception =>
        throw new XMPPAuctionException(
          "Could not create logger FileHandler " + FilenameUtils.getFullPath(LOG_FILE_NAME), e)
    }
  }
}


object XMPPAuctionHouse {
  val LOGGER_NAME = "auction-sniper"
  val LOG_FILE_NAME = "auction-sniper.log"
  val ITEM_ID_AS_LOGIN = "auction-%s"
  val AUCTION_RESOURCE = "Auction"
  val AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + XMPPAuctionHouse.AUCTION_RESOURCE

  def auctionId(itemId: String, connection: XMPPConnection) = 
    AUCTION_ID_FORMAT.format(itemId, connection.getServiceName)

  def connect(hostname: String, username: String, password:String): XMPPAuctionHouse = {
    val connection = new XMPPConnection(hostname) 
    try {
      connection.connect()
      connection.login(username, password, AUCTION_RESOURCE) 
      new XMPPAuctionHouse(connection)
    } catch {
      case xmppe: XMPPException =>
        throw new XMPPAuctionException("Could not connect to auction: " + connection, xmppe)
    }
  }
}
