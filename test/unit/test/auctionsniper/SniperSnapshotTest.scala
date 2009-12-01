package test.auctionsniper

import org.junit.Test
import org.junit.Assert._

import auctionsniper.{SniperSnapshot => Snapshot, SniperState => State}

class SniperSnapshotTest {
  
  @Test
  def transitionsBetweenStates() {
    val itemId = "item id"
    val joining = Snapshot.joining(itemId)
    
    assertEquals(new Snapshot(itemId, 0, 0, State.JOINING), joining)
    
    val bidding = joining.bidding(123, 234);
    
    assertEquals(new Snapshot(itemId, 123, 234, State.BIDDING), bidding)  
    assertEquals(new Snapshot(itemId, 456, 234, State.LOSING), bidding.losing(456))
    assertEquals(new Snapshot(itemId, 456, 234, State.WINNING), bidding.winning(456))  
    assertEquals(new Snapshot(itemId, 123, 234, State.LOST), bidding.closed())  
    assertEquals(new Snapshot(itemId, 678, 234, State.WON), bidding.winning(678).closed());  
  }

  @Test
  def comparesItemIdentities() {
    assertTrue(Snapshot.joining("item 1").isForSameItemAs(Snapshot.joining("item 1")))
    assertFalse(Snapshot.joining("item 1").isForSameItemAs (Snapshot.joining("item 2")))
  }
}
