package auctionsniper

import SniperState.*

case class SniperSnapshot(
  itemId: String,
  lastPrice: Int = 0,
  lastBid: Int = 0,
  state: SniperState = JOINING):

  def bidding(newLastPrice: Int, newLastBid: Int): SniperSnapshot =
    copy(lastPrice=newLastPrice, lastBid=newLastBid, state=BIDDING)

  def winning(newLastPrice: Int): SniperSnapshot = copy(lastPrice=newLastPrice, state=WINNING)

  def losing(newLastPrice: Int): SniperSnapshot = copy(lastPrice=newLastPrice, state=LOSING)

  def closed(): SniperSnapshot = copy(state = state.whenAuctionClosed)

  def failed(): SniperSnapshot = SniperSnapshot(itemId, state=FAILED)

  def isForSameItemAs(sniperSnapshot: SniperSnapshot): Boolean = itemId == sniperSnapshot.itemId


object SniperSnapshot:
  def joining(itemId: String): SniperSnapshot = SniperSnapshot(itemId)
