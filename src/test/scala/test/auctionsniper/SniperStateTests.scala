package test.auctionsniper

import auctionsniper.util.Defect

class SniperStateTests extends munit.FunSuite {
  
  import auctionsniper.SniperState._

  test("is won when auction closes while winning") {
    assertEquals(JOINING.whenAuctionClosed, LOST)
    assertEquals(BIDDING.whenAuctionClosed, LOST)
    assertEquals(WINNING.whenAuctionClosed, WON)
  }

  test("defect if auction closes when won") {
    intercept[Defect] {
      WON.whenAuctionClosed
    }
  }

  test("defect if auction closes when lost") {
    intercept[Defect] {
      LOST.whenAuctionClosed
    }
  }
}
