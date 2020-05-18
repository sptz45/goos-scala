package auctionsniper

trait Auction {
  def join()
  def bid(amount: Int)
  def addAuctionEventListener(listener: AuctionEventListener)
}
