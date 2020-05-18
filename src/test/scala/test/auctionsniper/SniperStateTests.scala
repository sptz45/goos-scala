package test.auctionsniper

import org.junit.Test
import org.junit.Assert._

import auctionsniper.util.Defect

class SniperStateTests {
  
  import auctionsniper.SniperState._ 

  @Test
  def isWonWhenAuctionClosesWhileWinning() {
    assertEquals(LOST, JOINING.whenAuctionClosed)
    assertEquals(LOST, BIDDING.whenAuctionClosed)
    assertEquals(WON,  WINNING.whenAuctionClosed)
  }
  
  @Test(expected=classOf[Defect])
  def defectIfAuctionClosesWhenWon() {
    WON.whenAuctionClosed
  }

  @Test(expected=classOf[Defect])
  def defectIfAuctionClosesWhenLost() {
    LOST.whenAuctionClosed
  }
}
