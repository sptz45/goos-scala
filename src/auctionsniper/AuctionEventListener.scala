package auctionsniper

import java.util.EventListener

trait AuctionEventListener extends EventListener {
  def auctionClosed()
  def currentPrice(price: Int , increment: Int, priceSource: PriceSource)
  def auctionFailed()
}

sealed class PriceSource private()

object PriceSource {
  object FromSniper extends PriceSource
  object FromOtherBidder extends PriceSource
}

