package test.auctionsniper

import auctionsniper.{SniperSnapshot => Snapshot, SniperState => State}

class SniperSnapshotTest extends munit.FunSuite:

  test("transitions between states") {
    val itemId = "item id"
    val joining = Snapshot.joining(itemId)
    
    assertEquals(joining, Snapshot(itemId, 0, 0, State.JOINING))
    
    val bidding = joining.bidding(123, 234)

    assertEquals(bidding, Snapshot(itemId, 123, 234, State.BIDDING))
    assertEquals(bidding.losing(456), Snapshot(itemId, 456, 234, State.LOSING))
    assertEquals(bidding.winning(456), Snapshot(itemId, 456, 234, State.WINNING))
    assertEquals(bidding.closed(), Snapshot(itemId, 123, 234, State.LOST))
    assertEquals(bidding.winning(678).closed(), Snapshot(itemId, 678, 234, State.WON))
  }


  test("compares item identities") {
    assert(Snapshot.joining("item 1").isForSameItemAs(Snapshot.joining("item 1")))
    assert(!Snapshot.joining("item 1").isForSameItemAs (Snapshot.joining("item 2")))
  }
