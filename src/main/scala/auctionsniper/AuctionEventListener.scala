package auctionsniper

import java.util.EventListener

trait AuctionEventListener extends EventListener {
  def auctionClosed(): Unit
  def currentPrice(price: Int , increment: Int, priceSource: PriceSource): Unit
  def auctionFailed(): Unit
}

sealed class PriceSource private()

object PriceSource {
  object FromSniper extends PriceSource
  object FromOtherBidder extends PriceSource
}

