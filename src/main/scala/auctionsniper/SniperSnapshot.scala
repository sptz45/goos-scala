package auctionsniper

import SniperState._

case class SniperSnapshot(
  itemId: String,
  lastPrice: Int = 0,
  lastBid: Int = 0,
  state: SniperState = JOINING) {

  def bidding(newLastPrice: Int, newLastBid: Int) =
    copy(lastPrice=newLastPrice, lastBid=newLastBid, state=BIDDING)

  def winning(newLastPrice: Int) = copy(lastPrice=newLastPrice, state=WINNING)

  def losing(newLastPrice: Int) = copy(lastPrice=newLastPrice, state=LOSING)

  def closed() = copy(state = state.whenAuctionClosed)

  def failed() = SniperSnapshot(itemId, state=FAILED)

  def isForSameItemAs(sniperSnapshot: SniperSnapshot) = itemId == sniperSnapshot.itemId
}

object SniperSnapshot {
  def joining(itemId: String) = SniperSnapshot(itemId)
}
