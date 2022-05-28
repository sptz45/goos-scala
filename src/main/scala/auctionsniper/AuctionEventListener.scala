package auctionsniper

import java.util.EventListener

trait AuctionEventListener extends EventListener:
  def auctionClosed(): Unit
  def currentPrice(price: Int , increment: Int, priceSource: PriceSource): Unit
  def auctionFailed(): Unit


enum PriceSource:
  case FromSniper
  case FromOtherBidder

