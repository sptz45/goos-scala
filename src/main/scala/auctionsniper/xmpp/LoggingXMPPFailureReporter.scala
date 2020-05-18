package auctionsniper.xmpp

import java.util.logging.Logger

class LoggingXMPPFailureReporter(logger: Logger) extends XMPPFailureReporter {
  import LoggingXMPPFailureReporter._
  
  def cannotTranslateMessage(auctionId: String, failedMessage: String, exception: Exception): Unit = {
    logger.severe(MESSAGE_FORMAT.format(auctionId, failedMessage, exception.toString))
  }
}

private object LoggingXMPPFailureReporter {
  val MESSAGE_FORMAT = "<%s> Could not translate message \"%s\" because \"%s\""
}
