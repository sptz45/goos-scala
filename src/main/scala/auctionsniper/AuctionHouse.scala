package auctionsniper

import UserRequestListener.Item

trait AuctionHouse {
  def auctionFor(item: Item): Auction
}
