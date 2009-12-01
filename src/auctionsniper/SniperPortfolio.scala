package auctionsniper

import java.util.EventListener
import scala.collection.mutable.ArrayBuffer
import util.Announcer

class SniperPortfolio extends SniperCollector {
  import SniperPortfolio._
  
  private val announcer = Announcer.to[PortfolioListener]
  private val snipers = new ArrayBuffer[AuctionSniper]
  
  def addSniper(sniper: AuctionSniper) {
    snipers += sniper
    announcer.announce().sniperAdded(sniper)
  }

  def addPortfolioListener(listener: PortfolioListener) {
    announcer += listener
  }
}

object SniperPortfolio { 
  trait PortfolioListener extends EventListener {
    def sniperAdded(sniper: AuctionSniper)
  }
}
