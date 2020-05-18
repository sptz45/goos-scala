package auctionsniper

import java.util.EventListener

trait SniperListener extends EventListener {
  def sniperStateChanged(snapshot: SniperSnapshot)
}
