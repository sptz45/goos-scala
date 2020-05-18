package auctionsniper.ui

import auctionsniper.SniperSnapshot

sealed abstract class Column(val name: String, val ordinal: Int) {
  def valueIn(snapshot: SniperSnapshot): Any
}

object Column {
  
  object ITEM_IDENTIFIER extends Column("Item", 0) {
    def valueIn(snapshot: SniperSnapshot): String = snapshot.itemId
  }
  
  object LAST_PRICE extends Column("Last Price", 1) {
    def valueIn(snapshot: SniperSnapshot): Int = snapshot.lastPrice
  }

  object LAST_BID extends Column("Last Bid", 2) {
    def valueIn(snapshot: SniperSnapshot): Int = snapshot.lastBid
  }
  
  object SNIPER_STATE extends Column("State", 3) {
    def valueIn(snapshot: SniperSnapshot): String = SnipersTableModel.textFor(snapshot.state)
  }
  
  val values = List(ITEM_IDENTIFIER, LAST_PRICE, LAST_BID, SNIPER_STATE)
  
  def at(offset: Int): Column = values(offset)
}