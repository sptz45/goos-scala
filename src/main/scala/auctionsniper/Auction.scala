package auctionsniper

trait Auction:
  def join(): Unit
  def bid(amount: Int): Unit
  def addAuctionEventListener(listener: AuctionEventListener): Unit

