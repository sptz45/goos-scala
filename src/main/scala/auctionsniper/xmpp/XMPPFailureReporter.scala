package auctionsniper.xmpp

trait XMPPFailureReporter {
  def cannotTranslateMessage(auctionId: String, failedMessage: String, exception: Exception): Unit
}
