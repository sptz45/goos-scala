package auctionsniper

import UserRequestListener.Item

class SniperLauncher(auctionHouse: AuctionHouse, collector: SniperCollector)
extends UserRequestListener {
  
  def joinAuction(item: Item): Unit = { 
    val auction = auctionHouse.auctionFor(item)
    val sniper = new AuctionSniper(item, auction) 
    auction.addAuctionEventListener(sniper)
    collector.addSniper(sniper)
    auction.join()
  }
}