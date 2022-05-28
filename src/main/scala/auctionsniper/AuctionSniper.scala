package auctionsniper

import util.Announcer
import UserRequestListener.Item

class AuctionSniper(item: Item, auction: Auction) extends AuctionEventListener:
  
  private val listeners = Announcer.to[SniperListener]
  var snapshot: SniperSnapshot = SniperSnapshot.joining(item.identifier)
  
  def addSniperListener(listener: SniperListener): Unit =
    listeners += listener
  
  def auctionClosed(): Unit =
    snapshot = snapshot.closed()
    notifyChange()
  
  def currentPrice(price: Int , increment: Int, priceSource: PriceSource): Unit =
    import PriceSource.*
    priceSource match
      case FromSniper =>
        snapshot = snapshot.winning(price)
      case _ =>
        val bid = price + increment
        if item.allowsBid(bid) then
          auction.bid(bid)
          snapshot = snapshot.bidding(price, bid)
        else
          snapshot = snapshot.losing(price)
    notifyChange()
  
  def auctionFailed(): Unit =
    snapshot = snapshot.failed()
    notifyChange()

  private def notifyChange(): Unit =
    listeners.announce().sniperStateChanged(snapshot)

