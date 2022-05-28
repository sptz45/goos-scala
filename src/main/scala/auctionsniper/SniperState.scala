package auctionsniper

import auctionsniper.util.Defect

enum SniperState:

  case JOINING
  case BIDDING
  case WINNING
  case LOSING

  // terminal states
  case LOST
  case WON
  case FAILED

  def whenAuctionClosed: SniperState = this match
    case JOINING => LOST
    case BIDDING => LOST
    case WINNING => WON
    case LOSING => LOST
    case LOST | WON | FAILED => throw Defect("Auction is already closed")
