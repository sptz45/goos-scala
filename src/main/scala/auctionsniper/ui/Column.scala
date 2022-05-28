package auctionsniper.ui

import auctionsniper.SniperSnapshot

enum Column(val name: String):
  case ITEM_IDENTIFIER extends Column("Item")
  case LAST_PRICE extends Column("Last Price")
  case LAST_BID extends Column("Last Bid")
  case SNIPER_STATE extends Column("State")

  def valueIn(snapshot: SniperSnapshot): Any = this match
    case Column.ITEM_IDENTIFIER => snapshot.itemId
    case Column.LAST_PRICE => snapshot.lastPrice
    case Column.LAST_BID => snapshot.lastBid
    case Column.SNIPER_STATE => SnipersTableModel.textFor(snapshot.state)

object Column:
  def at(offset: Int): Column = Column.values(offset)