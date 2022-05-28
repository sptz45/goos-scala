package auctionsniper.xmpp

import java.util.logging.Logger

class LoggingXMPPFailureReporter(logger: Logger) extends XMPPFailureReporter:

  def cannotTranslateMessage(auctionId: String, failedMessage: String, exception: Exception): Unit =
    logger.severe(s"<$auctionId> Could not translate message \"$failedMessage\" because \"$exception\"")

