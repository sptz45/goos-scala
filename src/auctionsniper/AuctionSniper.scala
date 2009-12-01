package auctionsniper

import util.Announcer
import UserRequestListener.Item

class AuctionSniper(item: Item, auction: Auction) extends AuctionEventListener {
  
  private val listeners = Announcer.to[SniperListener]
  var snapshot = SniperSnapshot.joining(item.identifier)
  
  def addSniperListener(listener: SniperListener) {
    listeners += listener
  }
  
  def auctionClosed() {
    snapshot = snapshot.closed()
    notifyChange()
  }
  
  def currentPrice(price: Int , increment: Int, priceSource: PriceSource) {
    import PriceSource._
    priceSource match {
      case FromSniper =>
        snapshot = snapshot.winning(price)
      case _ =>
        val bid = price + increment;
        if (item.allowsBid(bid)) {
          auction.bid(bid)
          snapshot = snapshot.bidding(price, bid)
        } else {
          snapshot = snapshot.losing(price)
        }
    }
    notifyChange()
  }
  
  def auctionFailed() {
    snapshot = snapshot.failed()
    notifyChange()
  }

  private def notifyChange() {
    listeners.announce().sniperStateChanged(snapshot)
  }
}
