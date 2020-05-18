package test.auctionsniper.ui

import org.junit.Test
import org.junit.Assert.assertEquals

import auctionsniper.{SniperSnapshot, SniperState}
import auctionsniper.ui.Column

class ColumnTest {

  @Test
  def retrievesValuesFromASniperSnapshot() {
    val snapshot = new SniperSnapshot("item", 123, 34, SniperState.BIDDING)
    assertEquals("item", Column.ITEM_IDENTIFIER.valueIn(snapshot))
    assertEquals(123, Column.LAST_PRICE.valueIn(snapshot))
    assertEquals(34, Column.LAST_BID.valueIn(snapshot))
    assertEquals("Bidding", Column.SNIPER_STATE.valueIn(snapshot))
  }
}
