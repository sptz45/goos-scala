package test.auctionsniper.ui

import auctionsniper.ui.Column
import auctionsniper.{SniperSnapshot, SniperState}

class ColumnTest extends munit.FunSuite {

  test("retrieves values from a SniperSnapshot") {
    val snapshot = new SniperSnapshot("item", 123, 34, SniperState.BIDDING)
    assertEquals(Column.ITEM_IDENTIFIER.valueIn(snapshot), "item")
    assertEquals(Column.LAST_PRICE.valueIn(snapshot), 123)
    assertEquals(Column.LAST_BID.valueIn(snapshot), 34)
    assertEquals(Column.SNIPER_STATE.valueIn(snapshot), "Bidding")
  }
}
